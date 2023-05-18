package com.example.eventbusactivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.eventbusactivity.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val _tag: String = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setupEventsListeners()
    }


    private fun setupEventsListeners() {
        subscribeClickEvent(1)

        subscribeTextChangeEvent(1)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                EventBusObj.subscribe<String> {
                    Log.d(_tag, "setupEventsListeners: $it")
                }
            }
        }

        val clickListener = OnClickListener {
            Log.d(_tag, "onClickListener: ${it.id} ")

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    ListenClickEvent.invokeEvent(it)
                    Log.d(_tag, "OnClickListener: setupEventsListeners $it")
                    EventBusObj.publish("Namaste!")
                }
            }
        }

        binding.buttonHello.setOnClickListener(clickListener)
        binding.buttonHi.setOnClickListener(clickListener)

        binding.buttonCreateStack.setOnClickListener {
            startActivity(Intent(this@MainActivity, NextActivity::class.java))
        }
    }

    private fun subscribeTextChangeEvent(unit: Int) {
        Log.d(_tag, "subscribeTextChangeEvent: $unit")
        lifecycleScope.launchWhenResumed {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                ListenUiChangeEvent.events.collectLatest {
                    Log.d(_tag, "subscribeTextChangeEvent $unit: UI change invoked for text: $it")
                    runOnUiThread {
                        binding.textViewGreeting.text = it
                    }
                }
            }
        }
    }

    private fun subscribeClickEvent(unit: Int) {
        Log.d(_tag, "subscribeClickEvent: $unit")

        lifecycleScope.launchWhenResumed {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                ListenClickEvent.events.collectLatest {
                    Log.d(_tag, "subscribeClickEvent $unit: click invoked for view ${it.id}")
                    val hiWorld = "Hi World!"
                    val helloWorld = "Hello World!"
                    val text = when (it.id) {
                        binding.buttonHi.id -> hiWorld
                        binding.buttonHello.id -> helloWorld
                        else -> ""
                    }
                    ListenUiChangeEvent.invokeEvent(text)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(_tag, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(_tag, "onDestroy: ")
        binding.unbind()
    }
}