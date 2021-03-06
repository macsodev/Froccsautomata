package hu.macsodev.froccsautomata;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TextView tvAranyAllas;
    SeekBar sbMennyiseg;
    SeekBar sbArany;
    Button kuldes;
    FloatingActionMenu fabmenu;
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice device;
    UUID PORT_UUID;
    boolean found;
    String arduinoBtName;
    TextView statusz;
    Boolean btPerm;
    ImageView ivPohar;
    Animation animFadeOut;
    Animation animFadeIn;

    Button On, Off, Discnt, Abt;
    String address = null;
    private Set<BluetoothDevice> pairedDevices;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvAranyAllas = findViewById(R.id.textView_arany_allas);
        sbMennyiseg = findViewById(R.id.seekbar_mennyiseg);
        sbArany = findViewById(R.id.seekbar_arany);
        kuldes = findViewById(R.id.button_kuldes);
        fabmenu=findViewById(R.id.menu);
        statusz = findViewById(R.id.tv_status);
        ivPohar = findViewById(R.id.iv_pohar);

        arduinoBtName = "HC-05";

        Intent newint = getIntent();
        address = newint.getStringExtra("device_a"); //receive the address of the bluetooth device
        found = false;
        btPerm = false;

        //new ConnectBT().execute(); //Call the class to connect

        animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fab_slide_in_from_left);

        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fab_slide_out_to_right);

        beallitasok();

        //new ConnectBT().execute(); //Call the class to connect
        //btSettings();

        //new ConnectBT().execute(); //Call the class to connect
        //btSettings2();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void beallitasok(){
        csuszkaModosit();

        sbArany.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                csuszkaModosit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbMennyiseg.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                csuszkaModosit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void menuItemSelected(View view) {
        switch (view.getId()) {
            case R.id.menu_item_hazmester:
                sbMennyiseg.setProgress(5-1);
                sbArany.setProgress(60);
                Snackbar.make(view, "Házmester beállítva.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                break;
            case R.id.menu_item_neszmelyi:
                sbMennyiseg.setProgress(5-1);
                sbArany.setProgress(100);
                Snackbar.make(view, "Neszmélyi beállítva.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                break;
            case R.id.menu_item_punci:
                sbMennyiseg.setProgress(5-1);
                sbArany.setProgress(33);
                Snackbar.make(view, "Puncilögybölő beállítva.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                break;

            case R.id.menu_item_nagyfroccs:
                sbArany.setProgress(66);
                sbMennyiseg.setProgress(3-1);   //0-tól van a kezdés
                Snackbar.make(view, "Nagyfröccs beállítva.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                break;
        }

        fabmenu.close(true);
    }
    public void csuszkaModosit(){
        int arany = sbArany.getProgress();
        int arany_szoda = sbArany.getMax()-arany;
        int mennyiseg = sbMennyiseg.getProgress() + 1;
        float mennyiseg_bor = ((float)mennyiseg*arany)/100;

        //bor mennyiseg 1 tizedesjegyre
        BigDecimal bd_bor = new BigDecimal(mennyiseg_bor);
        bd_bor = bd_bor.setScale(1, RoundingMode.HALF_UP);
        double mennyiseg_bor_bd = bd_bor.doubleValue();

        //szoda mennyiseg 1 tizedesjegyre
        BigDecimal bd_szoda = new BigDecimal(mennyiseg - mennyiseg_bor_bd);
        bd_szoda = bd_szoda.setScale(1, RoundingMode.HALF_UP);
        double mennyiseg_szoda_bd = bd_szoda.doubleValue();

        tvAranyAllas.setText(mennyiseg+"dl fröccs, "+arany+"% ("+ mennyiseg_bor_bd +" dl) bor / "+arany_szoda+"% ("+ mennyiseg_szoda_bd + " dl) szóda");

        // IV pohar beallitasok
        setIvPoharArany(arany);

    }

    public void setIvPoharArany(int arany){
        if(arany<10) ivPohar.setImageResource(R.mipmap.glass_arany_progress_0_fg);
        else if(arany>=10 && arany <20) ivPohar.setImageResource(R.mipmap.glass_arany_progress_10_fg);
        else if(arany >=20 && arany<30) ivPohar.setImageResource(R.mipmap.glass_arany_progress_20_fg);
        else if(arany >=30 && arany<40) ivPohar.setImageResource(R.mipmap.glass_arany_progress_30_fg);
        else if(arany >=40 && arany<50) ivPohar.setImageResource(R.mipmap.glass_arany_progress_40_fg);
        else if(arany >=50 && arany<60) ivPohar.setImageResource(R.mipmap.glass_arany_progress_50_fg);
        else if(arany >=60 && arany<70) ivPohar.setImageResource(R.mipmap.glass_arany_progress_60_fg);
        else if(arany >=70 && arany<80) ivPohar.setImageResource(R.mipmap.glass_arany_progress_70_fg);
        else if(arany >=80 && arany<90) ivPohar.setImageResource(R.mipmap.glass_arany_progress_80_fg);
        else if(arany >=90 && arany<100) ivPohar.setImageResource(R.mipmap.glass_arany_progress_90_fg);
        else if(arany >=100) ivPohar.setImageResource(R.mipmap.glass_arany_progress_100_fg);
    }

    public int btSettings2(){
        // init.
        isBtConnected = false;

        // BT adapter lekerese
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        // ha nincs BT adapter...
        if(myBluetooth == null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            return 0;
        }
        // ha nincs bekapcsolva a BT
        else if(!myBluetooth.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(turnBTon,1);

            // regi Android verziok jogkerese workaround
            if (Build.VERSION.SDK_INT > 22){
                String[] allPermissionNeeded = {
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_PRIVILEGED};
                List<String> permissionNeeded = new ArrayList<>();
                for (String permission : allPermissionNeeded)
                    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                        permissionNeeded.add(permission);
                if (permissionNeeded.size() > 0) {
                    requestPermissions(permissionNeeded.toArray(new String[0]), 0);
                }
            }
        }

        // parositott eszkozok lekerese
        pairedDevices = myBluetooth.getBondedDevices();

        // ha vannak parositott eszkozok...
        if (pairedDevices.size()>0){
            for(BluetoothDevice bt : pairedDevices)
            {
                // ha a parositott eszkoz neve megegyezik az elore definialt ARDUINO BT eszkoz nevevel...
                if(bt.getName().equals(arduinoBtName)) address = bt.getAddress();
            }
        }
        else{
            //nincsenek parositott eszkozok
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
            return 0;
        }

        // csatlakozas megkiserlese
        try
        {
            if (btSocket == null || !isBtConnected)
            {
                BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);         //connects to the device's address and checks if it's available
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);   //create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();//start connection
                isBtConnected = btSocket.isConnected();
            }
        }
        catch (IOException e)
        {
            //ConnectSuccess = false;//if the try failed, you can check the exception here
            return 0;
        }


        if(isBtConnected) return 1;
        else return 0;
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        Log.d("HIBA","onRequestPermissionsResult-ban vagyunk.");
        //Toast.makeText(getApplicationContext(),"onRequestPermissionsResult-ban vagyunk.",Toast.LENGTH_SHORT).show();

        //btSettings();

        /*if(hasAllPermissionsGranted(grantResults)){
            // all permissions granted
            Log.d("HIBA","permissions granted ");
            btPerm = true;

        }else {
            // some permission are denied.
            Log.d("HIBA","permissions denied ");
            statusz.setText("Bluetooth jog megtagadva");
        }
        */

    }


    public void tvStatuszOnClick(View view) {
        int retval = btSettings2();

        if(retval == 1) statusz.setText("Csatlakozva.");
        else statusz.setText("Nincs kapcsolat.");
    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public void BTKuldes(View view)
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("TESZT".getBytes(),0,5);
            }
            catch (IOException e)
            {
                Toast.makeText(this.getApplicationContext(),"BT ERROR", Toast.LENGTH_LONG);
            }
        }
    }
}



