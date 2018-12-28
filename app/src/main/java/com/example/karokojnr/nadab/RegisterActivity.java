package com.example.karokojnr.nadab;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.karokojnr.nadab.api.HotelService;
import com.example.karokojnr.nadab.api.RetrofitInstance;
import com.example.karokojnr.nadab.model.Hotel;
import com.example.karokojnr.nadab.model.HotelRegister;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText mobileNumber, businessName, applicantName, paybillNumber, address, businessEmail, city, password, passwordAgain;
    Button addHotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_register );
//        TODO:: initi UI components

        mobileNumber = (EditText)findViewById ( R.id.mobileNumber );
        businessName = (EditText)findViewById ( R.id.businessName );
        applicantName = (EditText)findViewById ( R.id.applicantName );
        paybillNumber = (EditText)findViewById ( R.id.paybillNumber );
        address = (EditText)findViewById ( R.id.address );
        businessEmail = (EditText)findViewById ( R.id.businessEmail );
        city = (EditText)findViewById ( R.id.city );
        password = (EditText)findViewById ( R.id.password );
        passwordAgain = (EditText)findViewById ( R.id.passwordAgain );

        addHotel = (Button)findViewById ( R.id.addHotel );


    }

    public void addHotel(View view) {

        // display a progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setCancelable(false); // set cancelable to false
        progressDialog.setMessage("Please Wait"); // set message
        progressDialog.show(); // show progress dialog

        HotelService service = RetrofitInstance.getRetrofitInstance().create(HotelService.class);
        Hotel hotel = new Hotel();



        String mmobileNumber = mobileNumber.getText ().toString();
        String mbusinessName = businessName.getText().toString();
        String mapplicantName = applicantName.getText().toString();
        String mpaybillNumber = paybillNumber.getText().toString();
        String maddress = address.getText().toString();
        String mbusinessEmail = businessEmail.getText().toString();
        String mcity = city.getText().toString();
        String mpassword = password.getText().toString();
        String mpasswordAgain = passwordAgain.getText().toString();


        // TODO:: Fetch fields from form
        hotel.setApplicantName(mapplicantName);
        hotel.setBusinessEmail(mbusinessEmail);
        hotel.setBusinessName(mbusinessName);
        hotel.setAddress(maddress);
        hotel.setCity(mcity);
        hotel.setMobileNumber( Integer.parseInt ( mmobileNumber ) );
        hotel.setPayBillNo(mpaybillNumber);
        hotel.setPassword(mpassword);
        // TODO :: Remove all the hard coded values

        Call<HotelRegister> call = service.addHotel(hotel);

        call.enqueue(new Callback<HotelRegister>() {
            @Override
            public void onResponse(Call<HotelRegister> call, Response<HotelRegister> response) {

                if(response.isSuccessful()) {
                    Log.d("JOA", "Hotel:: "+response.body().getHotel().toString());
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                }
                else
                    Toast.makeText(RegisterActivity.this,   "Error adding...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<HotelRegister> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Something went wrong...Error message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
