package hu.macsodev.froccsautomata;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
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

import abak.tr.com.boxedverticalseekbar.BoxedVertical;

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

    BoxedVertical bvArany;

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




        bvArany = (BoxedVertical)findViewById(R.id.bv_arany);

        bvArany.setOnBoxedPointsChangeListener(new BoxedVertical.OnValuesChangeListener() {
            @Override
            public void onPointsChanged(BoxedVertical boxedPoints, final int value) {
                bvOnPointsChanged();
            }

            @Override
            public void onStartTrackingTouch(BoxedVertical boxedPoints) {
                Toast.makeText(MainActivity.this, "onStartTrackingTouch", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(BoxedVertical boxedPoints) {
                Toast.makeText(MainActivity.this, "onStopTrackingTouch", Toast.LENGTH_SHORT).show();
            }
        });





        arduinoBtName = "froccsautomata";
        found = false;
        btPerm = false;

        beallitasok();
        btSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void bvOnPointsChanged(){
        int arany = bvArany.getValue();
        int arany_szoda = bvArany.getMax()-arany;
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
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

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
    }

    public int btSettings(){
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID

        if (bluetoothAdapter == null) {

            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
            statusz.setText("A telefon nem támogatja a Bluetooth-t!");
            return 0;
        }

        if(!bluetoothAdapter.isEnabled())   //ha van adapter -> jog kerese
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //requestPermissions();
            startActivityForResult(enableAdapter, 0);
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

        Set bondedDevices = bluetoothAdapter.getBondedDevices();



        if(bondedDevices.isEmpty()){
            //Toast.makeText(getApplicationContext(),"Először párosítani kell az eszközt!",Toast.LENGTH_SHORT).show();
            Log.d("HIBA","bondedDevices.isEmpty()");
            statusz.setText("Nincs kapcsolat");
        } else {

            for (Iterator<BluetoothDevice> iterator = bondedDevices.iterator(); iterator.hasNext();) {
                BluetoothDevice bt = iterator.next();
                if(bt.getName().equals(arduinoBtName)) //Replace with iterator.getName() if comparing Device names.
                {
                    device=bt; //device is an object of type BluetoothDevice
                    found=true;
                    break;
                }
            }
            if(found){  //megvan a BT eszkoz
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(PORT_UUID);
                    bluetoothSocket.connect();
                    Toast.makeText(getApplicationContext(),"Eszköz csatlakoztatva.",Toast.LENGTH_SHORT).show();
                    statusz.setText("Kapcsolódva");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(getApplicationContext(),"Nem található az eszköz.",Toast.LENGTH_SHORT).show();
                statusz.setText("Nincs kapcsolat");

            }
        }
        return 1;
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        Log.d("HIBA","onRequestPermissionsResult-ban vagyunk.");
        //Toast.makeText(getApplicationContext(),"onRequestPermissionsResult-ban vagyunk.",Toast.LENGTH_SHORT).show();

        btSettings();

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
        btSettings();
    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}
