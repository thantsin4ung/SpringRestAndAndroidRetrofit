package com.soft.restapp

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.soft.restapp.model.Product
import com.soft.restapp.model.StatusResponseEntity
import com.soft.restapp.service.RetrofitService
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val productAdapter = ProductAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dialog = setUpNewProductListItemDialog()
        floatingActionButton.setOnClickListener {
            dialog.show()
        }

        swiperefresh.apply {
            isEnabled = true
        }
        recyclerView.apply {
            setHasFixedSize(true)
            adapter = productAdapter
        }

    }

    private fun setUpNewProductListItemDialog(): AlertDialog {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_alert_dialog_create_item, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setTitle("Add new Product Item")
            .setPositiveButton("OK") { dialog, _ -> postNewProductItem(dialog, view) }
            .setNegativeButton("Cancel") {dialog, _ -> dialog.cancel() }
        val dialog = alertDialogBuilder.setView(view).create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    private fun postNewProductItem(dialog: DialogInterface, view: View) {

        val edTitle = view.findViewById<EditText>(R.id.editText)
        val edAvailable = view.findViewById<CheckBox>(R.id.checkBox2)
        val product = Product(edTitle.text.toString(), edAvailable.isChecked)

        swiperefresh.isRefreshing = true

        RetrofitService.factoryService().addProductItem(product)
            .enqueue( object : Callback<StatusResponseEntity<Product>?> {
                override fun onFailure(call: Call<StatusResponseEntity<Product>?>, t: Throwable) {
                    swiperefresh.isRefreshing = false
                    Toast.makeText(this@MainActivity, "Check your internet connection. ", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<StatusResponseEntity<Product>?>,
                    response: Response<StatusResponseEntity<Product>?>
                ) {
                    swiperefresh.isRefreshing = false
                    if (response.body() != null && response.body()?.status == true) {
                        productAdapter.add(response.body()!!.entity!!)
                    } else {
                        Toast.makeText(this@MainActivity, "Something went terribly wrong ", Toast.LENGTH_LONG).show()
                    }
                }
            })
        dialog.dismiss()

    }

    private fun loadProductFromServer(){
        swiperefresh.isRefreshing = true
        RetrofitService.factoryService().getProductList().enqueue( object : Callback<List<Product>?> {
            override fun onFailure(call: Call<List<Product>?>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Something went wrong please check your network!", Toast.LENGTH_LONG).show()
                swiperefresh.isRefreshing = false
            }

            override fun onResponse(
                call: Call<List<Product>?>,
                response: Response<List<Product>?>
            ) {
                swiperefresh.isRefreshing = false
                val items = response.body()
                if(items != null) {
                    productAdapter.addAll(items)
                }
            }
        })
    }
}
