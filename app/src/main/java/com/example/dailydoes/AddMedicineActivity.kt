package com.example.dailydoes

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class AddMedicineActivity : AppCompatActivity() {

    private lateinit var llTimesContainer: LinearLayout
    private val addedTimes = mutableListOf<String>()
    
    private var isEditMode = false
    private var medicineId = -1
    private lateinit var db: MYDB

    private lateinit var etName: EditText
    private lateinit var etDose: EditText
    private lateinit var etStartDate: EditText
    private lateinit var etNotes: EditText
    private lateinit var spFrequency: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medicine)

        initViews()
        db = MYDB(this)

        if (intent.getBooleanExtra("edit_mode", false)) setupEditMode()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        etStartDate.setOnClickListener { showDatePicker() }
        findViewById<Button>(R.id.btnAddTime).setOnClickListener { showTimePicker { time -> addTimeRow(time) } }
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveMedicine() }
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etDose = findViewById(R.id.etDose)
        etStartDate = findViewById(R.id.etStartDate)
        etNotes = findViewById(R.id.etNotes)
        spFrequency = findViewById(R.id.spFrequency)
        llTimesContainer = findViewById(R.id.llTimesContainer)

        spFrequency.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("مرة واحدة", "مرتين", "3 مرات", "4 مرات")
        )
    }

    private fun setupEditMode() {
        isEditMode = true
        medicineId = intent.getIntExtra("id", -1)
        etName.setText(intent.getStringExtra("name"))
        etDose.setText(intent.getStringExtra("dose"))
        spFrequency.setSelection(intent.getIntExtra("frequency", 1) - 1)
        etStartDate.setText(intent.getStringExtra("date"))
        etNotes.setText(intent.getStringExtra("notes") ?: "")

        intent.getStringExtra("times")?.split(",")?.forEach { 
            if (it.isNotBlank()) addTimeRow(it) 
        }
        findViewById<Button>(R.id.btnSave).text = "حفظ التعديلات"
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            etStartDate.setText("$d/${m + 1}/$y")
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(initialHour: Int = -1, initialMinute: Int = -1, onTimePicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        val h = if (initialHour != -1) initialHour else cal.get(Calendar.HOUR_OF_DAY)
        val m = if (initialMinute != -1) initialMinute else cal.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, hour, min ->
            onTimePicked(String.format("%02d:%02d", hour, min))
        }, h, m, true).show()
    }

    private fun saveMedicine() {
        val name = etName.text.toString()
        if (name.isEmpty() || addedTimes.isEmpty()) {
            Toast.makeText(this, "الرجاء إدخال اسم الدواء والوقت", Toast.LENGTH_SHORT).show()
            return
        }

        val dose = etDose.text.toString()
        val freq = spFrequency.selectedItemPosition + 1
        val date = etStartDate.text.toString()
        val times = addedTimes.joinToString(",")
        val notes = etNotes.text.toString()
        
        var id = medicineId.toLong()

        if (isEditMode) {
             db.updateMedicine(medicineId, name, dose, freq, date, times, notes)

             for(i in 0..50) AlarmUtils.cancelAlarm(this, medicineId * 100 + i)
        } else {
             id = db.insertMedicine(name, dose, freq, date, times, notes)
        }

        scheduleAlarms(id.toInt(), date, name, dose)

        Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun scheduleAlarms(medId: Int, dateStr: String, name: String, dose: String) {
        try {
            val dateParts = dateStr.split("/")
            val day = dateParts[0].toInt()
            val month = dateParts[1].toInt()
            val year = dateParts[2].toInt()
            
            for (i in addedTimes.indices) {
                val time = addedTimes[i]
                val timeParts = time.split(":")
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()
                

                val calendarStart = Calendar.getInstance()
                calendarStart.set(year, month - 1, day, hour, minute, 0)
                calendarStart.set(Calendar.MILLISECOND, 0)

                var triggerTime = calendarStart.timeInMillis
                val now = System.currentTimeMillis()

                if (triggerTime <= now) {
                    val calendarNext = Calendar.getInstance()
                    calendarNext.set(Calendar.HOUR_OF_DAY, hour)
                    calendarNext.set(Calendar.MINUTE, minute)
                    calendarNext.set(Calendar.SECOND, 0)
                    calendarNext.set(Calendar.MILLISECOND, 0)
                    
                    if (calendarNext.timeInMillis <= now) {

                        calendarNext.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    triggerTime = calendarNext.timeInMillis
                }

                val alarmId = medId * 100 + i
                AlarmUtils.scheduleAlarm(this, triggerTime, alarmId, name, dose)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addTimeRow(time: String) {
        addedTimes.add(time)


        val view = LinearLayout(this)
        view.orientation = LinearLayout.HORIZONTAL
        view.layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = 16 }
        view.setPadding(16, 16, 16, 16)
        view.setBackgroundResource(R.drawable.bg_input_gray)


        val tv = TextView(this)
        tv.text = "$time ${AlarmUtils.getTimeLabel(time)}"
        tv.textSize = 16f
        tv.setTextColor(Color.parseColor("#333333"))
        view.addView(tv, LinearLayout.LayoutParams(0, -2, 1f))


        val btnEdit = ImageView(this)
        btnEdit.setImageResource(R.drawable.ic_edit)
        btnEdit.setColorFilter(Color.parseColor("#4A6CF7"))
        btnEdit.setOnClickListener {
            val idx = llTimesContainer.indexOfChild(view)
            val parts = addedTimes[idx].split(":")
            showTimePicker(parts[0].toInt(), parts[1].toInt()) { newTime ->
                addedTimes[idx] = newTime
                tv.text = "$newTime ${AlarmUtils.getTimeLabel(newTime)}"
            }
        }
        view.addView(btnEdit, LinearLayout.LayoutParams(60, 60).apply { marginEnd = 16 })


        val btnDelete = ImageView(this)
        btnDelete.setImageResource(R.drawable.ic_delete)
        btnDelete.setColorFilter(Color.parseColor("#E53935"))
        btnDelete.setOnClickListener {
            val idx = llTimesContainer.indexOfChild(view)
            addedTimes.removeAt(idx)
            llTimesContainer.removeView(view)
        }
        view.addView(btnDelete, LinearLayout.LayoutParams(60, 60))

        llTimesContainer.addView(view)
    }
}
