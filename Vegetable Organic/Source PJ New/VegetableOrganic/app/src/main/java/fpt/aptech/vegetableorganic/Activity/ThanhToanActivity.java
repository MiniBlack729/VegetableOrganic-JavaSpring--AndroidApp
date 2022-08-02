package fpt.aptech.vegetableorganic.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import fpt.aptech.vegetableorganic.R;
import fpt.aptech.vegetableorganic.api.ApiClient;
import fpt.aptech.vegetableorganic.api.InterfaceApi;
import fpt.aptech.vegetableorganic.model.EventBus.TinhTongEvent;
import fpt.aptech.vegetableorganic.model.LoginRequest;
import fpt.aptech.vegetableorganic.model.Product;
import fpt.aptech.vegetableorganic.model.User;
import fpt.aptech.vegetableorganic.model.signinRequest;
import fpt.aptech.vegetableorganic.model.userCheckout;
import fpt.aptech.vegetableorganic.paypal.PayPalConfig;
import fpt.aptech.vegetableorganic.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThanhToanActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView txttongtien,txtsodt,txtemail;
    EditText editdiachi, editsdt;
    AppCompatButton btndathang, btnPaypal;
    ImageView imagehome;
    //Payment Amount
    private String paymentAmount;


    //Paypal intent request code to track onActivityResult method
    public static final int PAYPAL_REQUEST_CODE = 123;


    //Paypal Configuration Object
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);
        initView();
        initControl();
    }

    private void initControl() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
        DecimalFormat decimalFormat=new DecimalFormat("###,###,###");
        long tongtien=getIntent().getLongExtra("tongtien",0);
        //secsion
        User user = new User();
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("user",user.getEmail());
        txttongtien.setText(decimalFormat.format(tongtien)+" VNĐ");
        txtemail.setText(email);
        btndathang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_diachi=editdiachi.getText().toString().trim();
                String sdt = editsdt.getText().toString().trim();
//                String reg = "^(0)(\\s)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";
//                String regex ="/^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$/";

                if(TextUtils.isEmpty(sdt)){
                    Toast.makeText(getApplicationContext(), "Bạn chưa nhập số điện thoại", Toast.LENGTH_SHORT).show();
                }
                if(editsdt.getText().toString().trim().length()<10){
                    Toast.makeText(getApplicationContext(), "Nhập sai định dạng, vui lòng nhập số chính xác", Toast.LENGTH_SHORT).show();
                }
                if(TextUtils.isEmpty(str_diachi)){
                    Toast.makeText(getApplicationContext(), "Bạn chưa nhập địa chỉ", Toast.LENGTH_SHORT).show();
                }else{
                    userCheckout userCheckout1 = new userCheckout();
                    userCheckout1.setAddress(str_diachi);
                    userCheckout1.setPhone(sdt);
                    addCart(userCheckout1,email);

                    //post data
                    // Log.d("test",new Gson().toJson(Utils.manggiohang));
                }
            }
        });
        Intent intent = new Intent(ThanhToanActivity.this, PayPalService.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        startService(intent);
        btnPaypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPayment();

            }
        });
    }
    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    private void getPayment() {
        //Getting the amount from editText
        //paymentAmount = editdiachi.getText().toString();
        DecimalFormat decimalFormat=new DecimalFormat("###,###,###");
        long tongtien=getIntent().getLongExtra("tongtien",0);
        txttongtien.setText(decimalFormat.format(tongtien));

        //Creating a paypalpayment
        PayPalPayment payment = new PayPalPayment(new BigDecimal(tongtien/23000), "USD", "Pay Amount",
                PayPalPayment.PAYMENT_INTENT_SALE);

        //Creating Paypal Payment activity intent
        Intent intent = new Intent(this, PaymentActivity.class);

        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        //Puting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        //Starting the intent activity for result
        //the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the result is from paypal
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                //if confirmation is not null
                if (confirm != null) {
                    try {
                        //Getting the payment details
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        Log.i("paymentExample", paymentDetails);

                        //Starting a new activity for the payment details and also putting the payment details with intent


                        // Log.d("Success", paymentDetails);
                        User user = new User();
                        String str_diachi=editdiachi.getText().toString().trim();
                        String sdt = editsdt.getText().toString().trim();
                        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
                        String email = sharedPreferences.getString("user",user.getEmail());
                        userCheckout userCheckout1 = new userCheckout();
                        userCheckout1.setAddress(str_diachi);
                        userCheckout1.setPhone(sdt);
                        addCart(userCheckout1,email);

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    private void initView() {
        imagehome=findViewById(R.id.home);
        toolbar=findViewById(R.id.toolbar);
        txttongtien=findViewById(R.id.txttongtien);
        txtemail=findViewById(R.id.txtemail);

        editdiachi=findViewById(R.id.editdiachi);
        editsdt=findViewById(R.id.editsdt);
        btndathang=findViewById(R.id.btndathang);
        btnPaypal=findViewById(R.id.btnPaypal);
        imagehome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });
    }
    public void addCart(userCheckout userCheckout1, String email){

        InterfaceApi interfaceApi = ApiClient.getClient().create(InterfaceApi.class);

        Call<userCheckout> addProduct = interfaceApi.checkout(userCheckout1,email);
        addProduct.enqueue(new Callback<userCheckout>() {
            @Override
            public void onResponse(Call<userCheckout> call, Response<userCheckout> response) {
                if(response.isSuccessful()){

                    Toast.makeText(ThanhToanActivity.this,"Dat hang thanh cong",Toast.LENGTH_LONG).show();
                    Utils.manggiohang.clear();

                    EventBus.getDefault().postSticky(new TinhTongEvent());
                    startActivity(new Intent(ThanhToanActivity.this,MainActivity.class));

                }
            }

            @Override
            public void onFailure(Call<userCheckout> call, Throwable t) {

                String message = "Invalid";
                Toast.makeText(ThanhToanActivity.this,message,Toast.LENGTH_LONG).show();
            }
        });
    }
}