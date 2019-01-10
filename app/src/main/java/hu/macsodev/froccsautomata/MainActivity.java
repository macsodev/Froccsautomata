package hu.macsodev.froccsautomata;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.BlurEffect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.race604.drawable.wave.WaveDrawable;

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
    int ADAT_MENNYISEG;
    int ADAT_ARANY;

    Button On, Off, Discnt, Abt;
    String address = null;
    String name = null;
    private Set<BluetoothDevice> pairedDevices;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private RelativeLayout rl;
    private AppBarLayout abl;

    SweetSheet mSweetSheet;
    final ArrayList<MenuEntity> BTList = new ArrayList<>();

    ImageView imageView2;
    WaveDrawable chromeWave;

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
        btSettings2();

        rl = findViewById(R.id.rl);
        abl = findViewById(R.id.abl);
        setupRecyclerView();

        imageView2 = findViewById(R.id.iv_pohar);
        chromeWave = new WaveDrawable(this, R.drawable.glass_with_wine_launcher_circle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void setupRecyclerView() {
        // uj sweetsheet a relative layoutra
        mSweetSheet = new SweetSheet(rl);
        // bluetooth lista beallitasa a sheetre
        mSweetSheet.setMenuList(BTList);
        // recyclerView beallitasa
        mSweetSheet.setDelegate(new RecyclerViewDelegate(false));
        // hattereffekt beallitasa
        mSweetSheet.setBackgroundEffect(new BlurEffect(8));
        // menu item kattintasa eseten...
        mSweetSheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
            @Override
            public boolean onItemClick(int position, MenuEntity menuEntity1) {
                // Adott listaelem nevevel az adott BT cim beallitasa, majd a sweetsheet becsukasa
                if ((pairedDevices != null) && (pairedDevices.size()>0)){
                    for(BluetoothDevice bt : pairedDevices)
                    {
                        // ha a parositott eszkoz neve megegyezik az elore definialt ARDUINO BT eszkoz nevevel...

                        if(bt.getName().equals(menuEntity1.title)){
                            address = bt.getAddress();
                            name = bt.getName();
                            break;
                        }
                    }
                    btConnect();
                }


                mSweetSheet.toggle();
                if(mSweetSheet.isShow() == true) LayoutRendezesek(true);
                else LayoutRendezesek(false);

                return false;
            }
        });


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
        double[] aranyok = {0, 0.33, 0.5, 0.66, 1};

        int mennyiseg = sbMennyiseg.getProgress() + 1;
        int arany = sbArany.getProgress();
        double arany_szoda = mennyiseg - (mennyiseg * aranyok[arany]);//sbArany.getMax()-arany;
        double mennyiseg_bor = ((float)mennyiseg*aranyok[arany]);

        //bor mennyiseg 1 tizedesjegyre
        BigDecimal bd_bor = new BigDecimal(mennyiseg_bor);
        bd_bor = bd_bor.setScale(1, RoundingMode.HALF_UP);
        double mennyiseg_bor_bd = bd_bor.doubleValue();

        //szoda mennyiseg 1 tizedesjegyre
        BigDecimal bd_szoda = new BigDecimal(mennyiseg - mennyiseg_bor_bd);
        bd_szoda = bd_szoda.setScale(1, RoundingMode.HALF_UP);
        double mennyiseg_szoda_bd = bd_szoda.doubleValue();
        double bor_szazalek = (((mennyiseg*aranyok[arany]) / mennyiseg) * 100);
        arany_szoda = 100 - bor_szazalek;

        tvAranyAllas.setText(mennyiseg+"dl fröccs, "+ bor_szazalek +"% ("+ mennyiseg_bor_bd +" dl) bor / "+arany_szoda+"% ("+ mennyiseg_szoda_bd + " dl) szóda");

        // Kuldendo adatok beallitasa
        ADAT_MENNYISEG = mennyiseg;
        if(arany == 0) ADAT_ARANY = 8;
        else if(arany == 1) ADAT_ARANY = 4;
        else if(arany == 2) ADAT_ARANY = 5;
        else if(arany == 3) ADAT_ARANY = 6;
        else if(arany == 4) ADAT_ARANY = 7;

        StringBuilder sb = new StringBuilder();
        sb.append("ELKÜLDÖTT ADAT: ");
        sb.append(ADAT_MENNYISEG);
        sb.append(ADAT_ARANY);
        String s = sb.toString();

        Toast.makeText(this.getApplicationContext(), s, Toast.LENGTH_SHORT).show();

        // IV pohar beallitasok
        setIvPoharArany(arany);

    }

    public void setIvPoharArany(int arany){
        /*
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
        */

        if(arany==0) ivPohar.setImageResource(R.mipmap.glass_arany_progress_0_fg);
        else if(arany == 1) ivPohar.setImageResource(R.mipmap.glass_arany_progress_30_fg);
        else if(arany == 2) ivPohar.setImageResource(R.mipmap.glass_arany_progress_50_fg);
        else if(arany == 3) ivPohar.setImageResource(R.mipmap.glass_arany_progress_70_fg);
        else if(arany == 4) ivPohar.setImageResource(R.mipmap.glass_arany_progress_100_fg);

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
                MenuEntity me = new MenuEntity();
                me.iconId = R.drawable.glass;
                me.title = bt.getName();
                BTList.add(me);

                // DEFAULT ARDUINO BT NEV KIVALASZTASA CIMNEK, HA VAN
                if(bt.getName().equals(arduinoBtName)) {
                    address = bt.getAddress();
                    name = bt.getName();
                }
            }
        }
        else{
            //nincsenek parositott eszkozok
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
            return 0;
        }

        // csatlakozas megkiserlese
        btConnect();


        if(isBtConnected) return 1;
        else return 0;
    }

    public int btConnect(){
        statusz.setText("Kapcsolódás: " + name);

        try
        {
            if ((btSocket == null || !isBtConnected) && address!=null)
            {
                BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);         //connects to the device's address and checks if it's available
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);   //create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();//start connection
                isBtConnected = btSocket.isConnected();

                statusz.setText("Kapcsolódva: " + name);
            }
        }
        catch (IOException e)
        {
            //ConnectSuccess = false;//if the try failed, you can check the exception here
            statusz.setText("Nincs kapcsolat");
            return 0;
        }
        return 1;
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
        mSweetSheet.toggle();
        if(mSweetSheet.isShow() == true) LayoutRendezesek(true);
        else LayoutRendezesek(false);
        }

    public void LayoutRendezesek(boolean sweetbarfront){
        if(sweetbarfront == false){
            //rl.setClickable(false);
            rl.setVisibility(View.GONE);

            findViewById(R.id.cl).bringToFront();
            abl.bringToFront();
            findViewById(R.id.include).bringToFront();
            findViewById(R.id.menu).bringToFront();

        }
        else {
            rl.bringToFront();
            //rl.setClickable(true);
            rl.setVisibility(View.VISIBLE);

        }
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
        if (btSocket!=null && isBtConnected)
        {
            try
            {
                StringBuilder sb = new StringBuilder();
                sb.append(ADAT_MENNYISEG);
                sb.append(ADAT_ARANY);
                String s = sb.toString();

                // ADAT KULDESE
                btSocket.getOutputStream().write(s.getBytes(),0,5);
                Toast.makeText(this.getApplicationContext(),"ELKULDOTT ADAT: " + s, Toast.LENGTH_LONG);

            }
            catch (IOException e)
            {
                Toast.makeText(this.getApplicationContext(),"BT ERROR", Toast.LENGTH_LONG);
            }
        }




        imageView2.setImageDrawable(chromeWave);
        chromeWave.setLevel(5000);
        //chromeWave.setIndeterminate(true);
        new MyTask().execute();




    }

    public void onClickSweetSheet(View view) {
        if(mSweetSheet.isShow() == true) LayoutRendezesek(true);
        else LayoutRendezesek(false);
    }

    class MyTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            for(int i=0;10000>i;i+=50){

                try {
                    Thread.sleep(3);
                    publishProgress(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "Task Completed.";
        }
        @Override
        protected void onPostExecute(String result) {

        }
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            chromeWave.setLevel(values[0]);
        }
    }
}





