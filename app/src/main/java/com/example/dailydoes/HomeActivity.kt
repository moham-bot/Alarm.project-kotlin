package com.example.dailydoes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var db: MYDB
    private lateinit var data: ArrayList<HashMap<String, String>>
    private val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        listView = findViewById(R.id.listMedicines)
        db = MYDB(this)

        setupNavigation()
        
        findViewById<Button>(R.id.btnAddMedicine).setOnClickListener {
            startActivity(Intent(this, AddMedicineActivity::class.java))
        }
        findViewById<android.view.View>(R.id.btnChangeSound).setOnClickListener {
            startActivity(Intent(this, SoundActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        loadMedicines()
    }

    private fun setupNavigation() {
        findViewById<BottomNavigationView>(R.id.bottomMenu).setOnItemSelectedListener {
             when (it.itemId) {
                R.id.menu_home -> true
                R.id.menu_add -> {
                    startActivity(Intent(this, AddMedicineActivity::class.java))
                    true
                }
                R.id.menu_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadMedicines() {
        data = db.getAllMedicines()
        listView.adapter = MedicineAdapter(data)
    }

    private inner class MedicineAdapter(private val items: ArrayList<HashMap<String, String>>) : BaseAdapter() {
        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Any = items[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_medicine, parent, false)
            val item = items[position]
            val id = item["id"]?.toIntOrNull() ?: -1


            view.findViewById<TextView>(R.id.tvName).text = item["name"]
            view.findViewById<TextView>(R.id.tvDose).text = "(${item["dose"]})"
            view.findViewById<TextView>(R.id.tvDate).text = "البدء: ${item["date"]}"
            
            val tvNotes = view.findViewById<TextView>(R.id.tvNotes)
            tvNotes.text = "ملاحظة: ${item["notes"]}"
            tvNotes.visibility = if (item["notes"].isNullOrEmpty()) View.GONE else View.VISIBLE


            val container = view.findViewById<LinearLayout>(R.id.llDosesContainer)
            container.removeAllViews()
            
            val timeString = item["time"]
            if (timeString != null) {
                val times = timeString.split(",")
                for (i in times.indices) {
                    val time = times[i].trim()
                    if (time.isNotEmpty()) {
                        val doseText = TextView(this@HomeActivity)
                        doseText.text = "• جرعة ${i + 1}: $time ${AlarmUtils.getTimeLabel(time)}"
                        doseText.textSize = 14f
                        doseText.setTextColor(Color.parseColor("#555555"))
                        container.addView(doseText)
                    }
                }
            }


            val isTaken = item["last_taken_date"] == today
            val btnTaken = view.findViewById<View>(R.id.btnTaken)
            val imgTaken = view.findViewById<ImageView>(R.id.imgTaken)
            val tvTaken = view.findViewById<TextView>(R.id.tvTaken)

            if (isTaken) {
                imgTaken.setColorFilter(Color.parseColor("#43A047"))
                tvTaken.text = "تم أخذ الدواء اليوم"
                tvTaken.setTextColor(Color.parseColor("#43A047"))
                btnTaken.alpha = 1.0f
            } else {
                imgTaken.setColorFilter(Color.parseColor("#757575"))
                tvTaken.text = "تحديد كمأخوذ"
                tvTaken.setTextColor(Color.parseColor("#757575"))
                btnTaken.alpha = 0.7f
            }

            btnTaken.setOnClickListener {
                if (isTaken) {
                    Toast.makeText(this@HomeActivity, "تم تسجيله مسبقاً", Toast.LENGTH_SHORT).show()
                } else {
                    db.markAsTaken(id, today)
                    Toast.makeText(this@HomeActivity, "أحسنت! تم تسجيل الجرعة", Toast.LENGTH_SHORT).show()
                    loadMedicines()
                }
            }

            view.findViewById<View>(R.id.btnDelete).setOnClickListener {
                db.deleteMedicine(id)
                (0..10).forEach { AlarmUtils.cancelAlarm(this@HomeActivity, id * 100 + it) }
                loadMedicines()
                Toast.makeText(this@HomeActivity, "تم حذف الدواء", Toast.LENGTH_SHORT).show()
            }

            view.findViewById<View>(R.id.btnEdit).setOnClickListener {
                val intent = Intent(this@HomeActivity, AddMedicineActivity::class.java)
                intent.putExtra("edit_mode", true)
                intent.putExtra("id", id)
                intent.putExtra("name", item["name"])
                intent.putExtra("dose", item["dose"])
                
                val freqString = item["frequency"] ?: "1"
                intent.putExtra("frequency", freqString.toInt())
                
                intent.putExtra("date", item["date"])
                intent.putExtra("times", item["time"])
                intent.putExtra("notes", item["notes"])
                
                startActivity(intent)
            }

            return view
        }
    }
}