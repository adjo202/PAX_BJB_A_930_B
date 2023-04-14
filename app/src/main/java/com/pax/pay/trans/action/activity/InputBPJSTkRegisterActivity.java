package com.pax.pay.trans.action.activity;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.pax.abl.core.ActionResult;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.helper.MoneyTextWatcher;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.BPJSTkData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ToastUtils;
import com.pax.pay.utils.Utils;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;


@SuppressLint("SimpleDateFormat")
public class InputBPJSTkRegisterActivity extends BaseActivityWithTickForAction implements OnClickListener {
    public static final String TAG = "BPJSTkRegisterActivity";

    private Button confirmBtn;
    private EditText et_nik, et_nama_lengkap,et_email, etStartTime,etEndTime,et_tgl_lahir,et_domisili,et_hp,et_salary;
    private String navTitle, nik,customerName,hp, address, email,tglLahir, startTime,endTime,salary;

    private int selectedKantorBPJS = -1 , selectedJenisPekerjaan = -1,selectedJenisPekerjaan2 = -1, selectedLokasiPekerjaan = -1, selectedMonthProgram = -1,selectedFormatProgram = -1,selectedProvinsi = -1,selectedKabKot = -1;
    private Button sv_bpjs_location,bJob,bJob2,bJobLocation,month_program,format_program,sv_provinsi,sv_kab_kot;
    SpinnerDialog spinnerDialog,spinnerDialog2,spinnerDialog3,spinnerDialog4,spinnerDialog5,spinnerDialog6,spinnerDialog7,spinnerDialog8;
    private String preFillData;
    private int INDEX_FOR_NAME = 1;
    private int INDEX_FOR_CODE = 0;



    String kode_pekerjaan[][] = {
            {"P083", "AGEN46"},
            {"P073", "AHLI GIZI"},
            {"P042", "APOTEKER"},
            {"P038", "ARSITEK"},
            {"P061", "ARTIS"},
            {"P060", "ATLET"},
            {"P051", "BIARAWATI"},
            {"P041", "BIDAN"},
            {"P066", "BURUH BONGKAR MUAT/BAGASI"},
            {"P005", "BURUH HARIAN LEPAS"},
            {"P007", "BURUH NELAYAN/PERIKANAN"},
            {"P008", "BURUH PETERNAKAN"},
            {"P006", "BURUH TANI/PERKEBUNAN"},
            {"P040", "DOKTER"},
            {"P074", "DOKTER GIGI"},
            {"P034", "DOSEN"},
            {"P075", "FISIKAWAN MEDIK"},
            {"P035", "GURU"},
            {"P027", "IMAM MESJID"},
            {"P032", "JURU MASAK"},
            {"P062", "JURU PARKIR"},
            {"P039", "KONSULTAN"},
            {"P097", "MAHASISWA KERJA PRAKTEK"},
            {"P070", "MARBOT MESJID"},
            {"P021", "MEKANIK"},
            {"P053", "MITRA GOJEK"},
            {"P071", "MITRA GOJEK-GO LIFE"},
            {"P054", "MITRA GRAB"},
            {"P055", "MITRA UBER"},
            {"P059", "NARAPIDANA DALAM PROSES ASIMILASI"},
            {"P003", "NELAYAN/PERIKANAN"},
            {"P037", "NOTARIS"},
            {"P024", "PARAJI"},
            {"P049", "PARANORMAL"},
            {"P029", "PASTOR"},
            {"P050", "PEDAGANG"},
            {"P045", "PELAUT"},
            {"P064", "PEMANDU LAGU"},
            {"P009", "PEMBANTU RUMAH TANGGA"},
            {"P084", "PEMEGANG SAHAM"},
            {"P069", "PEMULUNG"},
            {"P019", "PENATA BUSANA"},
            {"P020", "PENATA RAMBUT"},
            {"P018", "PENATA RIAS"},
            {"P065", "PENDAMPING DESA"},
            {"P028", "PENDETA"},
            {"P046", "PENELITI"},
            {"P036", "PENGACARA"},
            {"P026", "PENTERJEMAH"},
            {"P044", "PENYIAR RADIO"},
            {"P025", "PERANCANG BUSANA"},
            {"P076", "PERAWAT"},
            {"P077", "PEREKAM MEDIK DAN INFOKES"},
            {"P072", "PESERTA BAKAT DAN MINAT"},
            {"P056", "PESERTA MAGANG"},
            {"P001", "PETANI/PEKEBUN"},
            {"P002", "PETERNAK"},
            {"P048", "PIALANG"},
            {"P033", "PROMOTOR ACARA"},
            {"P043", "PSIKIATER/PSIKOLOG"},
            {"P078", "PSIKOLOGI KLINIS"},
            {"P079", "RADIOGRAFER"},
            {"P067", "RELAWAN TAGANA/RELAWAN BENCANA"},
            {"P022", "SENIMAN"},
            {"P057", "SISWA KERJA PRAKTEK"},
            {"P047", "SOPIR"},
            {"P023", "TABIB"},
            {"P080", "TEKNISI LAB MEDIK"},
            {"P058", "TENAGA HONORER (SELAIN PENYELENGGARA NEGARA)"},
            {"P081", "TENAGA KESEHATAN LINGKUNGAN"},
            {"P082", "TENAGA TEKNIS KEFARMASIAN"},
            {"P004", "TRANSPORTASI"},
            {"P012", "TUKANG BATU"},
            {"P010", "TUKANG CUKUR"},
            {"P017", "TUKANG GIGI"},
            {"P016", "TUKANG JAHIT"},
            {"P013", "TUKANG KAYU"},
            {"P015", "TUKANG LAS/PANDAI BESI"},
            {"P011", "TUKANG LISTRIK"},
            {"P063", "TUKANG PIJAT"},
            {"P068", "TUKANG SAMPAH"},
            {"P014", "TUKANG SOL SEPATU"},
            {"P031", "USTADZ/MUBALIGH"},
            {"P030", "WARTAWAN"},
            {"P052", "WIRASWASTA"},
            {"P999", "LAIN-LAIN"}

    };


    String month_programs[][] = {
            {"1" , "1 Bulan"},
            {"2" , "2 Bulan"},
            {"3" , "3 Bulan"},
            {"6" , "6 Bulan"},
            {"12" , "12 Bulan"}
    };


    String format_programs[][] = {
            {"2" , "JKK, JKM"},
            {"3" , "JKK, JKM, JHT"}
    };


    ArrayList<String> listProvince;
    ArrayList<String> listKabKot;
    ArrayList<String> listWorkLocation;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_input_data_bpjs_tk_register;
    }

    @Override
    protected void loadParam() {

        preFillData = getIntent().getStringExtra(EUIParamKeys.CONTENT.toString());
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());

    }

    @Override
    protected void initViews() {
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);

        try {

            JSONObject pFillData = new JSONObject(preFillData);

            //Sandy : default
            String pNIK = pFillData.getString("nik");
            String pName = pFillData.getString("nama");
            String pNoHp = pFillData.getString("nomorHandphone");
            String pTglLahir = pFillData.getString("tanggalLahir");

            //new
            String pSalary = pFillData.has("salary") == false ? "" :
                    Utils.toMoneyFormat(pFillData.getString("salary"));

            String pEmail = pFillData.has("email") == false ? "" : pFillData.getString("email");
            String pDomisili = pFillData.has("alamat") == false ? "" : pFillData.getString("alamat");

            String pJamAwal = pFillData.has("jamAwal") == false ? "08:00" : pFillData.getString("jamAwal");
            String pJamAkhir = pFillData.has("jamAkhir") == false ? "17:00" : pFillData.getString("jamAkhir");
            /*
            String pJenisPekerjaanCode = pFillData.has("jenisPekerjaanCode") == false ? "" : pFillData.getString("jenisPekerjaanCode");
            String pLokasiBPJSCode = pFillData.has("lokasiBPJSCode") == false ? "" : pFillData.getString("lokasiBPJSCode");
            String pLokasiPekerjaanCode = pFillData.has("lokasiPekerjaanCode") == false ? "" : pFillData.getString("lokasiPekerjaanCode");
            String pPeriodeCode = pFillData.has("periodeCode") == false ? "" : pFillData.getString("periodeCode");
            String pProgramCode = pFillData.has("programCode") == false ? "" : pFillData.getString("programCode");
            */

            selectedJenisPekerjaan = pFillData.has("jenisPekerjaanIndex") == false ? -1 : Integer.parseInt(pFillData.getString("jenisPekerjaanIndex"));
            selectedKantorBPJS = pFillData.has("lokasiBPJSIndex") == false ? -1 : Integer.parseInt(pFillData.getString("lokasiBPJSIndex"));
            selectedLokasiPekerjaan = pFillData.has("lokasiPekerjaanIndex") == false ? -1 : Integer.parseInt(pFillData.getString("lokasiPekerjaanIndex"));
            selectedMonthProgram = pFillData.has("periodeIndex") == false ? -1 : Integer.parseInt(pFillData.getString("periodeIndex"));
            selectedFormatProgram = pFillData.has("programIndex") == false ? -1 : Integer.parseInt(pFillData.getString("programIndex"));

            String pjenisPekerjaan = pFillData.has("jenisPekerjaan") == false ? "" : pFillData.getString("jenisPekerjaan");
            String pLokasiBPJS = pFillData.has("lokasiBPJS") == false ? "" : pFillData.getString("lokasiBPJS");
            String pLokasiPekerjaan = pFillData.has("lokasiPekerjaan") == false ? "" : pFillData.getString("lokasiPekerjaan");
            String pPeriode = pFillData.has("periode") == false ? "" : pFillData.getString("periode");
            String pProgram = pFillData.has("program") == false ? "" : pFillData.getString("program");





            confirmBtn      = (Button) findViewById(R.id.lanjutkanBtn);

            et_nik          = (EditText) findViewById(R.id.et_nik);
            et_nik.setText(pNIK);
            et_nik.setEnabled(false);


            et_nama_lengkap = (EditText) findViewById(R.id.et_nama_lengkap);
            et_nama_lengkap.setText(pName);
            et_nama_lengkap.setEnabled(false);

            et_hp           = (EditText) findViewById(R.id.et_hp);
            et_hp.setText(pNoHp);
            et_hp.setEnabled(false);

            et_salary       = (EditText) findViewById(R.id.et_salary);
            et_salary.setText(pSalary);

            et_domisili     = (EditText) findViewById(R.id.et_domisili);
            et_domisili.setText(pDomisili);

            sv_bpjs_location = (Button) findViewById(R.id.sv_bpjs_location);
            sv_bpjs_location.setBackgroundColor(Color.WHITE);
            sv_bpjs_location.setText(pLokasiBPJS);

            month_program = (Button) findViewById(R.id.month_program);
            month_program.setBackgroundColor(Color.WHITE);
            month_program.setText(pPeriode);

            format_program = (Button) findViewById(R.id.format_program);
            format_program.setBackgroundColor(Color.WHITE);
            format_program.setText(pProgram);


            bJob = (Button) findViewById(R.id.sv_list_pekerjaan);
            bJob.setBackgroundColor(Color.WHITE);
            bJob.setText(pjenisPekerjaan);

            bJob2 = (Button) findViewById(R.id.sv_list_pekerjaan2);
            bJob2.setBackgroundColor(Color.WHITE);
            bJob2.setText(pjenisPekerjaan);









            /*
            sv_provinsi = (Button) findViewById(R.id.sv_provinsi);
            sv_provinsi.setBackgroundColor(Color.WHITE);

            sv_kab_kot = (Button) findViewById(R.id.sv_kab_kot);
            sv_kab_kot.setBackgroundColor(Color.WHITE);
            */

            bJobLocation = (Button) findViewById(R.id.sv_lokasi_pekerjaan);
            bJobLocation.setBackgroundColor(Color.WHITE);
            bJobLocation.setText(pLokasiPekerjaan);

            etStartTime = (EditText) findViewById(R.id.start_time);
            etStartTime.setBackgroundColor(Color.WHITE);
            etStartTime.setText(pJamAwal);

            etEndTime = (EditText) findViewById(R.id.end_time);
            etEndTime.setBackgroundColor(Color.WHITE);
            etEndTime.setText(pJamAkhir);

            et_email = (EditText) findViewById(R.id.et_email);
            et_email.setBackgroundColor(Color.WHITE);
            et_email.requestFocus();
            et_email.setText(pEmail);

            et_tgl_lahir = (EditText) findViewById(R.id.et_tgl_lahir);
            et_tgl_lahir.setBackgroundColor(Color.WHITE);
            et_tgl_lahir.setText(pTglLahir.replace("-",""));
            et_tgl_lahir.setEnabled(false);

        } catch (JSONException e) {
            e.printStackTrace();
        }




    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        listProvince    = new ArrayList<String>();
        listKabKot      = new ArrayList<String>();
        listWorkLocation = new ArrayList<String>();



        spinnerDialog = new SpinnerDialog(InputBPJSTkRegisterActivity.this, list_branch_office_bpjs_tk(),
                "Daftar Kantor BPJS");

        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);

        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                sv_bpjs_location.setText(item);
                selectedKantorBPJS = position;
            }
        });

        spinnerDialog2 = new SpinnerDialog(InputBPJSTkRegisterActivity.this, list_jenis_pekerjaan(),
                "Daftar Pekerjaan");

        spinnerDialog2.setCancellable(true);
        spinnerDialog2.setShowKeyboard(false);

        spinnerDialog2.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                bJob.setText(item);
                selectedJenisPekerjaan = position;
            }
        });


        spinnerDialog3 = new SpinnerDialog(InputBPJSTkRegisterActivity.this, list_lokasi_pekerjaan(),
                "Lokasi Pekerjaan");

        spinnerDialog3.setCancellable(true);
        spinnerDialog3.setShowKeyboard(false);

        spinnerDialog3.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                bJobLocation.setText(item);
                selectedLokasiPekerjaan = position;
            }
        });


        spinnerDialog4 = new SpinnerDialog(InputBPJSTkRegisterActivity.this, list_month_programs(),
                "Bulan Program");

        spinnerDialog4.setCancellable(true);
        spinnerDialog4.setShowKeyboard(false);

        spinnerDialog4.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                month_program.setText(item);
                selectedMonthProgram = position;
            }
        });


        spinnerDialog5 = new SpinnerDialog(InputBPJSTkRegisterActivity.this, list_format_programs(),
                "Pilih Program");

        spinnerDialog5.setCancellable(true);
        spinnerDialog5.setShowKeyboard(false);
        spinnerDialog5.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                format_program.setText(item);
                selectedFormatProgram = position;
            }
        });






        /*
        spinnerDialog6 = new SpinnerDialog(InputBPJSTkRegisterActivity.this, list_propinsi(),
                "Pilih Provinsi");


        spinnerDialog6.setCancellable(true);
        spinnerDialog6.setShowKeyboard(false);
        spinnerDialog6.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                    sv_provinsi.setText(item);
                    selectedProvinsi = position;
                    sv_kab_kot.setText("");

            }

        });


        spinnerDialog7 = new SpinnerDialog(InputBPJSTkRegisterActivity.this, list_kab_kot(),
                "Pilih Kabupaten Kota");

        spinnerDialog7.setCancellable(true);
        spinnerDialog7.setShowKeyboard(false);
        spinnerDialog7.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                sv_kab_kot.setText(item);
                selectedKabKot = position;
            }
        });

         */



        /*
        sv_provinsi = (Button) findViewById(R.id.sv_provinsi);
        sv_provinsi.setBackgroundColor(Color.WHITE);

        sv_kab_kot = (Button) findViewById(R.id.sv_kab_kot);
        sv_kab_kot.setBackgroundColor(Color.WHITE);
        */



        spinnerDialog8 = new SpinnerDialog(InputBPJSTkRegisterActivity.this, list_jenis_pekerjaan(),
                "Daftar Pekerjaan 2 (Optional)");
        spinnerDialog8.setCancellable(true);
        spinnerDialog8.setShowKeyboard(false);
        spinnerDialog8.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                bJob2.setText(item);
                selectedJenisPekerjaan2 = position;
            }
        });

    }




    private ArrayList<String> list_branch_office_bpjs_tk() {

        String branchOffices = FinancialApplication.getSysParam().get(SysParam.BPJS_BRANCH_OFFICE_DATA);
        try {
            JSONObject json = new JSONObject(branchOffices);
            JSONArray arr = json.getJSONArray("data");
            for (int i = 0; i < arr.length(); i++) {
                String sInfo = arr.getString(i);
                boolean isValidJson = Utils.isValidJSON(sInfo);
                if(isValidJson) {
                    JSONObject info = new JSONObject(sInfo);
                    String provinceName   = info.getString("namaPropinsi");
                    //String namaKabkot   = info.getString("namaKabkot");
                    String kantorCabang = info.getString("kantorCabang");
                    //listKabKot.add(String.format("%s / %s",provinceName, kantorCabang));
                    listKabKot.add(String.format("%s / %s",provinceName, kantorCabang));
                }
            }

            return listKabKot;
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;

    }

    private ArrayList<String> list_lokasi_pekerjaan() {

        try {
            String locationDatas = FinancialApplication.getSysParam().get(SysParam.BPJS_LOCATION_DATA);
            JSONObject json = new JSONObject(locationDatas);

            JSONArray arr = json.getJSONArray("data");
            int no = 1;
            for (int i = 0; i < arr.length(); i++) {
                String sInfo = arr.getString(i);
                JSONObject info = new JSONObject(sInfo);
                String workLocationName = info.getString("namaLokasi");
                String province = info.getString("namaPropinsi");
                String workLocationId   = info.getString("kodeLokasi");
                //listWorkLocation.add(String.format("%d.%s / %s", no, province,workLocationName));
                listWorkLocation.add(String.format("%s / %s", province,workLocationName));
                no++;
            }

            return listWorkLocation;


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }





    private ArrayList<String> list_jenis_pekerjaan() {


        ArrayList<String> list = new ArrayList<String>();
        int no = 1;
        for (int i = 0; i < kode_pekerjaan.length; i++) {
            //list.add(String.format("%d.%s",no, kode_pekerjaan[i][1]));
            list.add(String.format("%s", kode_pekerjaan[i][1]));
            no++;
        }
        return list;
    }



    private ArrayList<String> list_month_programs() {

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < month_programs.length; i++) {
            list.add(month_programs[i][1]);
        }
        return list;
    }



    private JSONObject getJSONPropinsiByIndex(int index, String data){
        String theData;
        if(data.equals(SysParam.BPJS_PROVINCE_DATA))
            theData = FinancialApplication.getSysParam().get(SysParam.BPJS_PROVINCE_DATA);
        else if(data.equals(SysParam.BPJS_DISTRICT_DATA))
            theData = FinancialApplication.getSysParam().get(SysParam.BPJS_DISTRICT_DATA);
        else if(data.equals(SysParam.BPJS_LOCATION_DATA))
            theData = FinancialApplication.getSysParam().get(SysParam.BPJS_LOCATION_DATA);
        else
            theData = FinancialApplication.getSysParam().get(SysParam.BPJS_BRANCH_OFFICE_DATA);


        try {
            JSONObject json = new JSONObject(theData);
            JSONArray arr = json.getJSONArray("data");
            for (int i = 0; i < arr.length(); i++) {
                String sInfo = arr.getString(i);
                boolean isValidJson = Utils.isValidJSON(sInfo);
                if(isValidJson && i == index)
                    return new JSONObject(sInfo);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }





    private ArrayList<String> list_kab_kot(){

        String districtData = FinancialApplication.getSysParam().get(SysParam.BPJS_DISTRICT_DATA);
        try {
            JSONObject json = new JSONObject(districtData);
            ArrayList<String> list = new ArrayList<String>();
            JSONArray arr = json.getJSONArray("data");
            int no = 1;
            for (int i = 0; i < arr.length(); i++) {
                String sInfo = arr.getString(i);
                boolean isValidJson = Utils.isValidJSON(sInfo);
                if(isValidJson){
                    JSONObject info = new JSONObject(sInfo);
                    String namaPropinsi = info.getString("namaPropinsi");
                    String namaKabkot = info.getString("namaKabkot");
                    //list.add(String.format("%d.%s / %s", no,namaPropinsi, namaKabkot));
                    list.add(String.format("%s / %s", namaPropinsi, namaKabkot));
                    no++;
                }

            }

            return list;


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;


    }


    private ArrayList<String> list_propinsi() {
        String provinceData = FinancialApplication.getSysParam().get(SysParam.BPJS_PROVINCE_DATA);
        try {
            JSONObject json = new JSONObject(provinceData);
            JSONArray arr = json.getJSONArray("data");
            int no = 1;
            for (int i = 0; i < arr.length(); i++) {
                String sInfo = arr.getString(i);
                boolean isValidJson = Utils.isValidJSON(sInfo);
                if(isValidJson) {
                    JSONObject info = new JSONObject(sInfo);
                    String namaPropinsi = info.getString("namaPropinsi");
                    //listProvince.add(String.format("%d.%s",no, namaPropinsi));
                    listProvince.add(String.format("%s", namaPropinsi));
                    no++;
                }
            }

            return listProvince;


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }







    private ArrayList<String> list_format_programs() {

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < format_programs.length; i++) {
            list.add(format_programs[i][1]);
        }
        return list;
    }




    @Override
    protected void setListeners() {
        confirmBtn.setOnClickListener(this);


        //format program
        format_program.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                spinnerDialog5.showSpinerDialog();
            }
        });

        //bulan program
        month_program.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                spinnerDialog4.showSpinerDialog();
            }
        });




        //kantor bpjs tk
        sv_bpjs_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog.showSpinerDialog();
            }
        });

        //job
        bJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog2.showSpinerDialog();
            }
        });

        //job 2
        bJob2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog8.showSpinerDialog();
            }
        });

        //job location
        bJobLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog3.showSpinerDialog();
            }
        });

        //start time
        etStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomTimePicker(R.id.start_time);
            }
        });

        etEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomTimePicker(R.id.end_time);
            }
        });

        //provinsi
        /*
        sv_provinsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog6.showSpinerDialog();
            }
        });
         */
        //kabupaten kota
        /*
        sv_kab_kot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //list_kab_kot();

                spinnerDialog7.showSpinerDialog();
            }
        });
        */

        et_salary.addTextChangedListener(new MoneyTextWatcher(et_salary));

    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    public void onClick(View v) {
        ActionResult result = null;
        switch (v.getId()) {
            case R.id.lanjutkanBtn:
                try {
                    String promptStr = proses();

                    //Sandy : disable temporary for testing purpose!
                    if (!TextUtils.isEmpty(promptStr)) {
                        ToastUtils.showMessage(InputBPJSTkRegisterActivity.this, promptStr);
                        return;
                    }

                    JSONObject workLocation         = getJSONPropinsiByIndex(selectedLokasiPekerjaan,SysParam.BPJS_LOCATION_DATA);
                    String sLokasiPekerjaan         = workLocation.getString("namaLokasi");
                    String sLokasiPekerjaanCode     = workLocation.getString("kodeLokasi");

                    JSONObject branchOffice         = getJSONPropinsiByIndex(selectedKantorBPJS,SysParam.BPJS_BRANCH_OFFICE_DATA);
                    String sKantorBPJS              = branchOffice.getString("kantorCabang");
                    String sKantorBPJSCode          = branchOffice.getString("kodeKantor");

                    String sKodePekerjaanCode       =  kode_pekerjaan[selectedJenisPekerjaan][INDEX_FOR_CODE];
                    String sPekerjaan               =  kode_pekerjaan[selectedJenisPekerjaan][INDEX_FOR_NAME];

                    String sKodePekerjaanCode2 = null;
                    String sPekerjaan2 = null;
                    if(selectedJenisPekerjaan2 != -1 ){
                        sKodePekerjaanCode2       =  kode_pekerjaan[selectedJenisPekerjaan2][INDEX_FOR_CODE];
                        sPekerjaan2               =  kode_pekerjaan[selectedJenisPekerjaan2][INDEX_FOR_NAME];
                    }




                    String sMonthProgram            =  month_programs[selectedMonthProgram][INDEX_FOR_NAME];
                    String sMonthProgramCode        =  month_programs[selectedMonthProgram][INDEX_FOR_CODE];

                    String sFormatProgram           =  format_programs[selectedFormatProgram][INDEX_FOR_NAME];
                    String sFormatProgramCode       =  format_programs[selectedFormatProgram][INDEX_FOR_CODE];


                    BPJSTkData bpjsData = new BPJSTkData(nik, customerName, email,
                                                address,
                                                tglLahir,
                                                hp,
                                                salary,
                                                sMonthProgram,
                                                sMonthProgramCode,
                                                sFormatProgram,
                                                sFormatProgramCode,
                                                sPekerjaan,
                                                sKodePekerjaanCode,
                                                sLokasiPekerjaan,
                                                sLokasiPekerjaanCode,
                                                sKantorBPJS,
                                                sKantorBPJSCode,
                                                startTime,
                                                endTime,
                                                sPekerjaan2,
                                                sKodePekerjaanCode2);



                    JSONObject store = new JSONObject();
                    store.put("nik",             bpjsData.getNik());
                    store.put("nomorHandphone",  bpjsData.getHp());
                    store.put("nama",            bpjsData.getCustomerName());
                    store.put("email",           bpjsData.getEmail());
                    store.put("tanggalLahir",    bpjsData.getBirthDate());
                    store.put("alamat",          bpjsData.getAddress());
                    store.put("jamAwal",         bpjsData.getStartTime());
                    store.put("jamAkhir",        bpjsData.getEndTime());
                    store.put("salary",          bpjsData.getSalary());

                    store.put("jenisPekerjaanCode",  bpjsData.getJobTypeCode());
                    store.put("jenisPekerjaanCode2",  bpjsData.getJobTypeCode2()); //optional

                    store.put("lokasiBPJSCode",      bpjsData.getBPJSLocationCode());
                    store.put("lokasiPekerjaanCode", bpjsData.getJobLocationCode());
                    store.put("periodeCode",         bpjsData.getMonthProgramCode());
                    store.put("programCode",         bpjsData.getFormatProgramCode());

                    store.put("jenisPekerjaanIndex",  selectedJenisPekerjaan);
                    store.put("lokasiBPJSIndex",      selectedKantorBPJS);
                    store.put("lokasiPekerjaanIndex", selectedLokasiPekerjaan);
                    store.put("periodeIndex",         selectedMonthProgram);
                    store.put("programIndex",         selectedFormatProgram);

                    store.put("jenisPekerjaan",      bpjsData.getJobType());
                    store.put("lokasiBPJS",          bpjsData.getBPJSLocation());
                    store.put("lokasiPekerjaan",     bpjsData.getJobLocation());
                    store.put("periode",             bpjsData.getMonthProgram());
                    store.put("program",             bpjsData.getFormatProgram());

                    //Sandy : store it in local prefs
                    FinancialApplication.getSysParam().set(SysParam.BPJS_REGISTER_REQUEST_DATA,store.toString());

                    result = new ActionResult(TransResult.SUCC, bpjsData);
                    finish(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

    private String proses() {
        String result = "";

        nik = et_nik.getText().toString();
        if (TextUtils.isEmpty(nik)) {
            result = "NIK Tidak Boleh Kosong";
            return result;
        }
        if (nik.length()!=16) {
            result = "Masukkan 16 Digit NIK";
            return result;
        }

        customerName = et_nama_lengkap.getText().toString();
        if (TextUtils.isEmpty(customerName)) {
            result = "Nama Tidak Boleh Kosong";
            return result;
        }

        //email is not mandatory
        email = et_email.getText().toString();
        if(Utils.isValidEmail(email) == Boolean.FALSE &&
                TextUtils.isEmpty(email) == Boolean.FALSE){
            result = "Email Tidak Valid";
            return result;
        }

        hp = et_hp.getText().toString();
        if (TextUtils.isEmpty(hp)) {
            result = "Nomor HP Tidak Boleh Kosong";
            return result;
        }




        address = et_domisili.getText().toString();
        if (TextUtils.isEmpty(address)) {
            result = "Domisili tidak Boleh Kosong";
            return result;
        }

        salary = et_salary.getText().toString();
        if (TextUtils.isEmpty(salary)) {
            result = "Upah tidak Boleh Kosong";
            return result;
        }

        salary = salary.replace("Rp","").replace(",","").replace(".","");
        if (Long.parseLong(salary) == 0 ) {
            result = "Upah tidak Boleh Rp.0";
            return result;
        }


        tglLahir = et_tgl_lahir.getText().toString();
       if (tglLahir.length() != 8) {
            result = "Format Tanggal Lahir Salah";
            et_tgl_lahir.setText("");
            return result;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        try {
            dateFormat.setLenient(false);
            dateFormat.parse(tglLahir);
        } catch (Exception e) {
            Log.e("teg", "", e);
            result = "Format Tanggal Lahir Salah";
            //et_tgl_lahir.setText("");
            return result;
        }

        String tgl = tglLahir.substring(0,2);
        String bln = tglLahir.substring(2,4);
        String year = tglLahir.substring(4,8);
        String completeDate = String.format("%s-%s-%s",tgl,bln,year);
        tglLahir = completeDate;



        if (selectedMonthProgram == -1) {
            result = "Silahkan Pilih Bulan Program";
            return result;
        }

        if (selectedFormatProgram == -1) {
            result = "Silahkan Pilih Program";
            return result;
        }




        if (selectedKantorBPJS == -1) {
            result = "Silahkan Pilih Kantor BPJS";
            return result;
        }

        if (selectedJenisPekerjaan == -1) {
            result = "Silahkan Pilih Jenis Pekerjaan";
            return result;
        }

        if (selectedLokasiPekerjaan == -1) {
            result = "Silahkan Pilih Lokasi Pekerjaan";
            return result;
        }


        startTime = etStartTime.getText().toString();
        if (TextUtils.isEmpty(startTime)) {
            result = "Jam awal Tidak Boleh Kosong";
            return result;
        }

        if (startTime.length() !=5 && startTime.contains(":") == Boolean.FALSE ) {
            result = "Jam awal tidak Valid";
            return result;
        }

        endTime = etEndTime.getText().toString();
        if (TextUtils.isEmpty(endTime)) {
            result = "Jam akhir Tidak Boleh Kosong";
            return result;
        }

        if (endTime.length() !=5 && endTime.contains(":") == Boolean.FALSE ) {
            result = "Jam akhir tidak Valid";
            return result;
        }

        if(Utils.checkTime(startTime,endTime) == Boolean.FALSE){
            result = "Jam awal dan akhir tidak valid";
            return result;
        }

        return result;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish( new ActionResult(TransResult.ERR_ABORTED, null));
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(new ActionResult(TransResult.ERR_ABORTED, null));

    }




    public void showCustomTimePicker(int templateId)
    {
        //default
        int hour = 0;
        int minute = 0;
        if(R.id.start_time == templateId){
            hour    = 8;
            minute  = 0;
        }else if (R.id.end_time == templateId){
            hour = 17;
            minute = 0;
        }

        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (view.isShown()) {
                    String theTime = String.format("%02d:%02d",hourOfDay,minute);
                    if(R.id.start_time == templateId) {
                        etStartTime.setText(theTime);
                    }else if (R.id.end_time == templateId){
                        etEndTime.setText(theTime);
                    }

                }
            }

        };

        CustomTimePickerDialog timePickerDialog = new CustomTimePickerDialog(
                InputBPJSTkRegisterActivity.this,
                myTimeListener,
                hour, minute, true);
        timePickerDialog.setTitle("Pilih Jam:");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();
    }






}



class CustomTimePickerDialog extends TimePickerDialog {

    private final static int TIME_PICKER_INTERVAL = 30;
    private TimePicker mTimePicker;
    private final OnTimeSetListener mTimeSetListener;

    public CustomTimePickerDialog(Context context, OnTimeSetListener listener,
                                  int hourOfDay, int minute, boolean is24HourView) {

        super(context, TimePickerDialog.THEME_HOLO_LIGHT, null,
                hourOfDay, minute / TIME_PICKER_INTERVAL, is24HourView);
        mTimeSetListener = listener;
    }

    @Override
    public void updateTime(int hourOfDay, int minuteOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minuteOfHour / TIME_PICKER_INTERVAL);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (mTimeSetListener != null) {
                    mTimeSetListener.onTimeSet(mTimePicker, mTimePicker.getCurrentHour(),
                            mTimePicker.getCurrentMinute() * TIME_PICKER_INTERVAL);
                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            Class<?> classForid = Class.forName("com.android.internal.R$id");
            Field timePickerField = classForid.getField("timePicker");
            mTimePicker = (TimePicker) findViewById(timePickerField.getInt(null));
            Field field = classForid.getField("minute");

            NumberPicker minuteSpinner = (NumberPicker) mTimePicker
                    .findViewById(field.getInt(null));
            minuteSpinner.setMinValue(0);
            minuteSpinner.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
            List<String> displayedValues = new ArrayList<>();
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(String.format("%02d", i));
            }
            minuteSpinner.setDisplayedValues(displayedValues
                    .toArray(new String[displayedValues.size()]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}







