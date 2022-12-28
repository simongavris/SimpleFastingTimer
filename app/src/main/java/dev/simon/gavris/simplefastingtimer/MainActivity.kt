package dev.simon.gavris.simplefastingtimer

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
        Locale.getDefault())

    // Declare a CountDownTimer object
    private lateinit var timer: CountDownTimer
    private var timerRunning: Boolean = false

    //var prefs = null
    private var timerMillis = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getPreferences(Context.MODE_PRIVATE)
        timerRunning = prefs.getBoolean("timerRunning", false)

        // Get a reference to the Button elements
        val stopButton = findViewById<Button>(R.id.stop_button)
        val setTimeButton = findViewById<Button>(R.id.set_time_button)

        if (timerRunning) {
            //if the timer was running prior to app being closed, resume
            //for this we need to find what the target datetime was
            //and calculate the milliseconds to that time
            val targetTimeStr = prefs.getString("targetTime", "") //todo: fix value not available
            val targetTime = dateFormatter.parse(targetTimeStr!!)
            timerMillis = getMillisToDate(targetTime!!)
            setTimerText(timerMillis)
            timer = createTimer(timerMillis)
            timer.start()
            stopButton.text = "Stop"

        } else {
            timerMillis = getMillisToDate(getTargetTimeFromDefaults())
            setTimerText(timerMillis)
            stopButton.text = "Start"
        }

        // Set an OnClickListener for the stop button
        stopButton.setOnClickListener {
            if (timerRunning) { //timer is currently running and user wants to stop
                timer.cancel()
                timerRunning = false
                stopButton.text = "Start"

                val editor = prefs.edit()
                editor.putBoolean("timerRunning", timerRunning)
                editor.apply()

                timerMillis = getMillisToDate(getTargetTimeFromDefaults())
                setTimerText(timerMillis)
            } else { //timer is currently stopped and user wants to start
                timerMillis = getMillisToDate(getTargetTimeFromDefaults())
                setTimerText(timerMillis)
                timer = createTimer(timerMillis)

                timer.start()
                timerRunning = true
                stopButton.text = "Stop"

                val calendar = Calendar.getInstance()

                // Add hours and minutes to the calendar
                calendar.add(Calendar.MILLISECOND, timerMillis.toInt())
                val modifiedDateTime = calendar.time
                val dateTimeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()).format(modifiedDateTime)

                val editor = prefs.edit()
                editor.putString("targetTime", dateTimeString)
                editor.putBoolean("timerRunning", true)
                editor.apply()
            }
        }

        // Set an OnClickListener for the set time button
        setTimeButton.setOnClickListener {
            timerMillis = selectTime()
        }
    }

    private fun getTargetTimeFromDefaults(): Date{
        val prefs = getPreferences(Context.MODE_PRIVATE)

        val timerSetHour = prefs.getInt("hour", 16)
        val timerSetMinute = prefs.getInt("minute", 0)

        val setDate = Calendar.getInstance()
        setDate.add(Calendar.HOUR_OF_DAY, timerSetHour)
        setDate.add(Calendar.MINUTE, timerSetMinute)

        return setDate.time
    }

    private fun selectTime(): Long {
        val view = layoutInflater.inflate(R.layout.dialog_time_select, null)
        val hoursInput = view.findViewById<EditText>(R.id.hours_input)
        val minutesInput = view.findViewById<EditText>(R.id.minutes_input)


        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Set Time")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val hours = hoursInput.text.toString().toInt()
                val minutes = minutesInput.text.toString().toInt()

                val prefs = getPreferences(Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putInt("hour", hours)
                editor.putInt("minute", minutes)
                editor.putBoolean("timerRunning", false)
                editor.apply()

                timer.cancel()
                timerRunning = false
                timerMillis = getMillisToDate(getTargetTimeFromDefaults())
                setTimerText(timerMillis)
                timer = createTimer(timerMillis)

                val stopButton = findViewById<Button>(R.id.stop_button)
                stopButton.text = "Start"
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
        alertDialog.show()
        return 0
    }

    private fun createTimer(duration: Long): CountDownTimer {
        return object: CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerRunning = true
                setTimerText(millisUntilFinished)
            }

            override fun onFinish() {
                // Update the UI when the countdown finishes
                val stopButton = findViewById<Button>(R.id.stop_button)
                stopButton.text = "Start"
                timerRunning = false
            }
        }

    }

    private fun getMillisToDate(date: Date): Long {
        val currentDate = Date()
        return date.time - currentDate.time
    }

    fun setTimerText(millisUntilFinished: Long) {
        // Convert the elapsed time to hours, minutes, and seconds
        val hours = (millisUntilFinished / 1000) / 3600
        val minutes = ((millisUntilFinished / 1000) % 3600) / 60
        val seconds = (millisUntilFinished / 1000) % 60

        // Update the countdown in the UI
        val countdownText = findViewById<TextView>(R.id.countdown_text)
        countdownText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
