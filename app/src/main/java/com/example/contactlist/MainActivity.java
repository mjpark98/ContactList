package com.example.contactlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.SaveDateListener {

    private Contact currentContact;
    final int PERMISSION_REQUEST_PHONE = 102;
    final int PERMISSION_REQUEST_CAMERA = 103;
    final int PERMISSION_REQUEST_MESSAGE = 104;
    final int CAMERA_REQUEST = 1888;
    //ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initListButton();
        initMapButton();
        initSettingsButton();
        initToggleButton();
        initChangeDateButton();
        initTextChangedEvents();
        initSaveButton();
        //initCallFunction();
        initMessageFunction();
        initImageButton();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            initContact(extras.getInt("contactid"));
        }
        else {
            currentContact = new Contact();
        }

    }
    private void initListButton() {
        ImageButton ibList = findViewById(R.id.imageButtonList);
        ibList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, ContactListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    private void initMapButton() {
        ImageButton ibMap = findViewById(R.id.imageButtonMap);
        ibMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, ContactMapActivity.class);
                if (currentContact.getContactID() == -1){
                    Toast.makeText(getBaseContext(), "Contact must be saved before it can be mapped",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    intent.putExtra("contactid", currentContact.getContactID());
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    private void initSettingsButton() {
        ImageButton ibSettings = findViewById(R.id.imageButtonSettings);
        ibSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, ContactSettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
//    private void initCallFunction(){
//        EditText editPhone = (EditText) findViewById(R.id.editHome);
//        editPhone.setOnLongClickListener(new View.OnLongClickListener(){
//            @Override
//            public boolean onLongClick(View arg0){
//                checkPhonePermission(currentContact.getPhoneNumber());
//                return false;
//            }
//        });
//        EditText editCell = (EditText) findViewById(R.id.editCell);
//        editCell.setOnLongClickListener(new View.OnLongClickListener(){
//            @Override
//            public boolean onLongClick(View arg0){
//                checkPhonePermission(currentContact.getCellNumber());
//                return false;
//            }
//        });
//    }
    private void initMessageFunction(){
        EditText editPhone = (EditText) findViewById(R.id.editHome);
        editPhone.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View arg0){
                checkMessagePermission(currentContact.getPhoneNumber());
                return false;
            }
        });
        EditText editCell = (EditText) findViewById(R.id.editCell);
        editCell.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View arg0){
                checkMessagePermission(currentContact.getCellNumber());
                return false;
            }
        });
    }


    private void initToggleButton(){
        final ToggleButton editToggle = (ToggleButton)findViewById(R.id.toggleButtonEdit);
        editToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                setForEditing(editToggle.isChecked());
            }
        });
    }
    private void initChangeDateButton(){
        Button changeDate = findViewById(R.id.btnBirthday);
        changeDate.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                DatePickerDialog datePickerDialog = new DatePickerDialog();
                datePickerDialog.show(fm, "DatePick");
            }
        });
    }
    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        EditText editName = findViewById(R.id.editName);
        imm.hideSoftInputFromWindow(editName.getWindowToken(), 0);
        EditText editAddress = findViewById(R.id.editAddress);
        imm.hideSoftInputFromWindow(editAddress.getWindowToken(), 0);
    }
    private void initSaveButton(){
        Button saveButton = findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                hideKeyboard();
                boolean wasSuccessful;
                ContactDataSource ds = new ContactDataSource(MainActivity.this);

                try{
                    ds.open();
                    if (currentContact.getContactID() == -1) {
                        wasSuccessful = ds.insertContact(currentContact);
                        int newID = ds.getLastContactID();
                        currentContact.setContactID(newID);
                    }
                    else {
                        wasSuccessful = ds.updateContact(currentContact);
                    }
                    ds.close();
                }
                catch (Exception e){
                    wasSuccessful = false;
                }
                if(wasSuccessful){
                    ToggleButton editToggle = findViewById(R.id.toggleButtonEdit);
                    editToggle.toggle();
                    setForEditing(false);
                }
            }
        }
        );
    }
    private void initImageButton(){
        ImageButton ib = findViewById(R.id.imageContact);
        ib.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23){
                    if(ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED){
                        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA)){
                            Snackbar.make(findViewById(R.id.activity_main),
                                    "The app needs permission to take pictures.",
                                    Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Ok", new View.OnClickListener(){
                                        @Override
                                        public void onClick(View view){
                                            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                                    {android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                                        }
                            })
                                    .show();
                        }else{
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.CAMERA},
                                    PERMISSION_REQUEST_CAMERA);
                        }
                    }
                    else{
                        takePhoto();
                    }
                }else{
                    takePhoto();
                }
            }
        });
    }

    private void initTextChangedEvents(){
        final EditText getContactName = findViewById(R.id.editName);
        getContactName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                currentContact.setContactName(getContactName.getText().toString());
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //autogenerated method stub
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // autogenerated method stub
            }
        });
        final EditText getStreetAddress = findViewById(R.id.editAddress);
        getStreetAddress.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s){
                currentContact.setStreetAddress(getStreetAddress.getText().toString());
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3){
                //autogenerated method stub
            }
            public void onTextChanged(CharSequence s, int start, int before, int count){
                //autogenerated method stub
            }
        });
        final EditText getCity = findViewById(R.id.editCity);
        getCity.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s){
                currentContact.setCity(getCity.getText().toString());
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3){
                //autogenerated method stub
            }
            public void onTextChanged(CharSequence s, int start, int before, int count){
                //autogenerated method stub
            }
        });
        final EditText getState = findViewById(R.id.editState);
        getState.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s){
                currentContact.setState(getState.getText().toString());
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3){
                //autogenerated method stub
            }
            public void onTextChanged(CharSequence s, int start, int before, int count){
                //autogenerated method stub
            }
        });
        final EditText getZipCode = findViewById(R.id.editZipcode);
        getZipCode.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s){
                currentContact.setZipCode(getZipCode.getText().toString());
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3){
                //autogenerated method stub
            }
            public void onTextChanged(CharSequence s, int start, int before, int count){
                //autogenerated method stub
            }
        });
        final EditText getPhoneNumber = findViewById(R.id.editHome);
        getPhoneNumber.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s){
                currentContact.setPhoneNumber(getPhoneNumber.getText().toString());
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3){
                //autogenerated method stub
            }
            public void onTextChanged(CharSequence s, int start, int before, int count){
                //autogenerated method stub
            }
        });
        final EditText getCellNumber = findViewById(R.id.editCell);
        getCellNumber.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s){
                currentContact.setCellNumber(getCellNumber.getText().toString());
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3){
                //autogenerated method stub
            }
            public void onTextChanged(CharSequence s, int start, int before, int count){
                //autogenerated method stub
            }
        });
        final EditText geteMail = findViewById(R.id.editEmail);
        geteMail.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s){
                currentContact.seteMail(geteMail.getText().toString());
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3){
                //autogenerated method stub
            }
            public void onTextChanged(CharSequence s, int start, int before, int count){
                //autogenerated method stub
            }
        });
        getPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        getCellNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }
    private void setForEditing(boolean enabled){
        ImageButton picture = findViewById(R.id.imageContact);
        picture.setEnabled(enabled);
        EditText editName = findViewById(R.id.editName);
        EditText editAddress = findViewById(R.id.editAddress);
        EditText editCity = findViewById(R.id.editCity);
        EditText editState = findViewById(R.id.editState);
        EditText editZipCode = findViewById(R.id.editZipcode);
        EditText editPhone = findViewById(R.id.editHome);
        EditText editCell = findViewById(R.id.editCell);
        EditText editEmail = findViewById(R.id.editEmail);
        Button buttonChange = findViewById(R.id.btnBirthday);
        Button buttonSave = findViewById(R.id.buttonSave);

        editName.setEnabled(enabled);
        editAddress.setEnabled(enabled);
        editCity.setEnabled(enabled);
        editState.setEnabled(enabled);
        editZipCode.setEnabled(enabled);
        editEmail.setEnabled(enabled);
        buttonChange.setEnabled(enabled);
        buttonSave.setEnabled(enabled);

        if(enabled){
            editName.requestFocus();
            editPhone.setInputType(InputType.TYPE_CLASS_PHONE);
            editCell.setInputType(InputType.TYPE_CLASS_PHONE);
        }
        else{
            editPhone.setInputType(InputType.TYPE_NULL);
            editCell.setInputType(InputType.TYPE_NULL);
        }
    }
    @Override
    public void didFinishDatePickerDialog(Calendar selectedTime) {
        TextView birthDay = findViewById(R.id.textBirthday);
        birthDay.setText(DateFormat.format("MM/dd/yyyy", selectedTime));
        currentContact.setBirthday(selectedTime);
    }
    //populates main activity by retrieving data in the database
    private void initContact(int ID){
        ContactDataSource ds = new ContactDataSource(MainActivity.this);
        ImageButton picture = (ImageButton) findViewById(R.id.imageContact);
        try{
            ds.open();
            currentContact = ds.getSpecificContact(ID);
            if(currentContact.getPicture() != null){
                picture.setImageBitmap(currentContact.getPicture());
            }
            else{
                picture.setImageResource(R.drawable.contactpng); //need icon pic from prof
            }
            ds.close();
        }
        catch (Exception e){
            Toast.makeText(this, "Load Contact Failed", Toast.LENGTH_LONG).show();
        }
        EditText editName = findViewById(R.id.editName);
        EditText editAddress = findViewById(R.id.editAddress);
        EditText editCity = findViewById(R.id.editCity);
        EditText editState = findViewById(R.id.editState);
        EditText editZipCode = findViewById(R.id.editZipcode);
        EditText editPhone = findViewById(R.id.editHome);
        EditText editCell = findViewById(R.id.editCell);
        EditText editEmail = findViewById(R.id.editEmail);
        TextView birthDay = findViewById(R.id.textBirthday);

        editName.setText(currentContact.getContactName());
        editAddress.setText(currentContact.getStreetAddress());
        editCity.setText(currentContact.getCity());
        editState.setText(currentContact.getState());
        editZipCode.setText(currentContact.getZipCode());

        editPhone.setText(currentContact.getPhoneNumber());
        editCell.setText(currentContact.getCellNumber());
        editEmail.setText(currentContact.geteMail());
        birthDay.setText(DateFormat.format("MM/dd/yyyy", currentContact.getBirthday().getTimeInMillis()).toString());
    }
//    private void checkPhonePermission (String phoneNumber){
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (ContextCompat.checkSelfPermission(MainActivity.this,
//                    android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
//                        android.Manifest.permission.CALL_PHONE)) {
//                    Snackbar.make(findViewById(R.id.activity_main),
//                            "MyContactList requires this permission to place a call from the app.",
//                            Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            ActivityCompat.requestPermissions(
//                                    MainActivity.this,
//                                    new String[]{
//                                            Manifest.permission.CALL_PHONE},
//                                    PERMISSION_REQUEST_PHONE);
//                        }
//                    })
//                            .show();
//                } else {
//                    ActivityCompat.requestPermissions(MainActivity.this, new
//                                    String[]{android.Manifest.permission.CALL_PHONE},
//                            PERMISSION_REQUEST_PHONE);
//                }
//            } else {
//                callContact(phoneNumber);
//            }
//        }
//        else{
//            callContact(phoneNumber);
//        }
//    }
private void checkMessagePermission (String phoneNumber){
    if (Build.VERSION.SDK_INT >= 23) {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.SEND_SMS)) {
                Snackbar.make(findViewById(R.id.activity_main),
                        "MyContactList requires this permission to message from the app.",
                        Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{
                                        Manifest.permission.SEND_SMS},
                                PERMISSION_REQUEST_MESSAGE);
                    }
                })
                        .show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new
                                String[]{android.Manifest.permission.SEND_SMS},
                        PERMISSION_REQUEST_MESSAGE);
            }
        } else {
            messageContact(phoneNumber);
        }
    }
    else{
        messageContact(phoneNumber);
    }
}
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
//            case PERMISSION_REQUEST_PHONE: {
//                if (grantResults.length > 0 && grantResults[0] ==
//                        PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(MainActivity.this, "You may now call from this app.",
//                            Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "You will not be able to make calls " +
//                            "from this app", Toast.LENGTH_LONG).show();
//                }
//            }
            case PERMISSION_REQUEST_MESSAGE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "You may now message from this app.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "You will not be able to send messages " +
                            "from this app", Toast.LENGTH_LONG).show();
                }
            }
            case PERMISSION_REQUEST_CAMERA: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takePhoto();
                } else{
                    Toast.makeText(MainActivity.this, "You will not be able to save contact pictures from this app",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
//    private void callContact(String phoneNumber){
//        Intent intent = new Intent(Intent.ACTION_CALL);
//        intent.setData(Uri.parse("tel:" + phoneNumber));
//        if(Build.VERSION.SDK_INT >= 23 &&
//                ContextCompat.checkSelfPermission(getBaseContext(),
//                        Manifest.permission.CALL_PHONE) !=
//                        PackageManager.PERMISSION_GRANTED){
//            return;
//        }
//        else{
//            startActivity(intent);
//        }
//    }
private void messageContact(String phoneNumber){
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setData(Uri.parse("tel:" + phoneNumber));
    if(Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.SEND_SMS) !=
                    PackageManager.PERMISSION_GRANTED){
        return;
    }
    else{
        startActivity(intent);
    }
}
    public void takePhoto(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Bitmap scaledPhoto = Bitmap.createScaledBitmap(photo, 144, 144, true);
                ImageButton ImageContact = (ImageButton) findViewById(R.id.imageContact);
                ImageContact.setImageBitmap(scaledPhoto);
                currentContact.setPicture(scaledPhoto);
            }
        }
    }

}