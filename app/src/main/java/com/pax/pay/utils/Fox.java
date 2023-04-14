package com.pax.pay.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.pax.dal.IPrinter;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.dal.exceptions.PrinterDevException;
import com.pax.eemv.IEmv;
import com.pax.eemv.entity.AidParam;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ProductData;
import com.pax.pay.trans.model.TransData;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;
import static java.lang.Float.parseFloat;
import static java.lang.Math.max;

/**
 * Created by Sugeng on 3/1/2017.
 */

public class Fox {

    static AidParam aidParam;

    public static String Masking(String input, int idxAddStr, String Append, int start, int end, String Change){
        int cnt=1, cntstr = start;

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (i == idxAddStr * cnt) {
                result.append(Append);
                cnt++;
            }

            if((i >= cntstr) && ((i < end))){
                result.append(Change);
            }else {
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }

    public static int bitTest(String BitString, int YangKe)
    {
        int Rv=0;
        int sisa = YangKe % 4;
        int IndexKe = YangKe/4;
        if(sisa>0){IndexKe=IndexKe+1;};
        String NibelTest = upper( com.pax.pay.utils.Fox.Substr(BitString,IndexKe,1));
        if (sisa==0 && at(NibelTest,"13579BDF")>0) Rv=1;
        if (sisa==1 && at(NibelTest,"89ABCDEF")>0) Rv=1;
        if (sisa==2 && at(NibelTest,"4567CDEF")>0) Rv=1;
        if (sisa==3 && at(NibelTest,"2367ABEF")>0) Rv=1;
        return Rv;
    }


    public static byte[] num2lenbnd(int len)
    {
        byte [] RV=new byte [2];
        String xx= right("0000"+trim(str(len)),4);
        RV[0] = (byte) Hex2Txt(left(xx,2)).charAt(0);
        RV[1] = (byte) Hex2Txt(right(xx,2)).charAt(0);
        return RV;
    }


        public  static int bin2num(String bin, int len) {
        //*******************************************************
        int ik, num = 0;
        for (ik = len; ik >= 1; ik=ik-1)
        {
            if(ik == len ) {
                num = asc(Substr(bin, ik, 1)) * (int) Math.pow(256,(len-ik));
            }
            else
            {
            num = num + asc(Substr(bin, ik, 1)) * (int) Math.pow(256,(len-ik));
            }
        }
        return num;
    }


    public  static String angka2Bcd(int Angka)
    //*********************************
    {
        String Rv;
        int Kanan, Kiri;
        Kanan=Angka % 256;
        Kiri = (Angka-Kanan)/256;
        Rv = "" + chr(Kiri) + chr(Kanan);
        return Rv;
    }

    public  static int asc(String X)
    {
        int ascii = (int) X.charAt(0) & 0xff;
        return ascii;
    }


    public  static int asc(byte X)
    {
        int ascii = (int) X & 0xff ;
        return ascii;
    }

    public  static int asc(char X)
    {
        char character = 'a';
        int ascii = (int) X & 0xff;
        return ascii;
    }


    public  static String trimnul(String cTxt)
    {
        int Panjang=len(cTxt);
        String hasil="";
        int i;
        for (i = 1; i<=Panjang;i++)
        {
        if(!Txt2Hex(Substr(cTxt, i, 1)).equals("00")){ hasil = hasil + Substr(cTxt, i, 1); }
        }
        return hasil;
    }


    public   static int word2num(String txt)
    {
        return asc(Substr(txt,1,1))*256 + asc(Substr(txt,2,1));
    }


    public  static String trimff(String cTxt)
    {
        int Panjang=len(cTxt);
        String hasil="";
        int i;
        int v;
        byte x;
        for (i = 1; i<=Panjang;i++)
        {
            x = (byte) Substr(cTxt, i, 1).charAt(0);
            v= x & 0xff;
            if(v!=255){ hasil = hasil + Substr(cTxt, i, 1); }
        }
        return hasil;
    }


    public  static int occurs(String Dicari, String Sumber )
    {
        return Sumber.split(Dicari).length;
    }


    public static String trim(String strx)
    {
        return strx.trim();
    }

    public  static String Xchrtran(String Asli, String Dibuang, String Diganti)
    {
        String result;

        result = Asli.replace("[", "");
        result = result.replace("]", "");
        return result;
    }


    public  static String chrtran(String Asli, String Dibuang, String Diganti)
    {
        String result=Asli;
        int panjang = len(Dibuang);
        String Pengganti;
        for (int k = 0;k<panjang;k++)
        {
            Pengganti="";
            if(len(Diganti)>k){Pengganti=Substr(Diganti,k+1,1);}
            result = result.replace(Substr(Dibuang,k+1,1), Pengganti);
        }
        return result;
    }


    public  static float valFloat(String Huruf)
    {
        return parseFloat(Huruf);
    }

    public static String str(int Dt)
    {
        return Integer.toString(Dt);
    }

    public static String Txt2Hex(String Txt) {
        String RVTxt2Hex = "";
        int LTxt = Txt.length();
        int xn, p1, p2,m,k;
        int n;
        String x;

        for (int g = 1; g <= LTxt; g++) {
            x = Substr(Txt, g, 1);
            xn = (int) x.charAt(0);
            n = xn & 0xFF;

            if(n<=255)
            {
                p1 = (int) (n / 16) + 1;
                p2 = (int) (n % 16) + 1;
                RVTxt2Hex = RVTxt2Hex + Substr("0123456789ABCDEF", p1, 1) + Substr("0123456789ABCDEF", p2, 1);
            }
            else
            {
                m = (int) n%256;
                k = (int) n/256;
                p1 = (int) (k / 16) + 1;
                p2 = (k % 16) + 1;
                RVTxt2Hex = RVTxt2Hex + Substr("0123456789ABCDEF", p1, 1) + Substr("0123456789ABCDEF", p2, 1);

                p1 = (int) (m / 16) + 1;
                p2 = (m % 16) + 1;
                RVTxt2Hex = RVTxt2Hex + Substr("0123456789ABCDEF", p1, 1) + Substr("0123456789ABCDEF", p2, 1);
            }
            RVTxt2Hex=RVTxt2Hex;
        }
        return RVTxt2Hex;
    }


    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String paddingKiri(String s, int n) {
        return StringUtils.leftPad(s, n, " ");
    }

    public static String paddingKanan(String s, int n) {
        return StringUtils.rightPad(s, n, " ");
    }


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();


    public  static String upper(String txt)
    {
        return txt.toUpperCase();
    }

    public  static String lower(String txt)
    {
        return txt.toLowerCase();
    }

    public  static int Hex2Num(String cHex)
    {
        cHex=cHex.toUpperCase();
        cHex=cHex.trim();
        int nn=cHex.length();
        if( (nn%2)==1)
        {
            cHex='0'+cHex;
        }
        int kiri  = "0123456789ABCDEF".indexOf(left(cHex,1));
        int kanan = "0123456789ABCDEF".indexOf(right(cHex,1));
        int rv=max(kiri,0)*16+max(kanan,0);
        return rv;
    }



    public  static int at(String dicari, String Sumber)  // CHECK LAGI sama yang lain kebalik param nya
    {
        int posi  = Sumber.indexOf(dicari)+1;
        return posi;
    }



    public static String AscHex2Txt(String tHex)
    {
        String HexNya = Txt2Hex(tHex);
        String RVHex2Txt="";

        int l=tHex.length();
        for (int i=1;i<=l;i++)
        {
            RVHex2Txt=RVHex2Txt+chr(val(Substr(HexNya,(i-1)*2+1,2)));
        }
        return RVHex2Txt;
    }



    public static String Hex2Txt(String tHex)
    {
        String RVHex2Txt="";
        int l=tHex.length(),num;
        String dHex="",depan="";
        for (int i=1;i<=l;i++)
        {
            if(Substr(tHex,i,1)!=" ")
            {
                dHex=dHex+Substr(tHex,i,1);
            }
        }
        if (dHex.length()%2==1)
        {
            dHex="0"+dHex;
        }
        while (dHex!="")
        {
            depan = left(dHex,2);
            dHex=stuff(dHex,1,2,"");
            num = Hex2Num(depan);
            Character x=(char) num;
            RVHex2Txt=RVHex2Txt+x;
        }
        return RVHex2Txt;
    }


    public  static String Replicate(String x, int times)
    {
        String Rv = new String(new char[times]).replace("\0", x);
        return Rv;
    }

    public  static String Space(int times)
    {
        String Rv = new String(new char[times]).replace("\0", " ");
        return Rv;
    }


    public static String Substr(String Txt, int mulai, int Sepanjang)
    {
        if (len(Txt) < mulai + Sepanjang){Sepanjang = len(Txt)-mulai+1;}
        return Txt.substring(mulai-1,mulai-1+Sepanjang);
    }


    public  static String left(String Dt, int panjang)
    {
        if(len(Dt)<=panjang) return Dt;
        String Rv = Dt.substring(0,panjang);
        return Rv;
    }

    public  static String right(String Rstring, int panjang)
    {
        if(len(Rstring)<=panjang) return Rstring;
        int l=Rstring.length();
        return Rstring.substring(l-panjang,l);
    }

    public  static double iif(boolean cond, double benar, double salah)
    {
        if(cond) return benar;
        return salah;
    }


    public  static String iif(boolean cond, String benar, String salah)
    {
        if(cond) return benar;
        return salah;
    }

    public  static int iif(boolean cond, int benar, int salah)
    {
        if(cond) return benar;
        return salah;
    }

    public  static boolean between(double arg1, double bawah, double atas)
    {
        if(arg1<bawah || arg1>atas) return false;
        return true;
    }

    public  static int len(String x)
    //***************************
    {
        return x.length();
    }

    public  static char chr(int n)
    //***************************
    {
        return (char) n;
    }

    public  static String CHR(int n)
    //***************************
    {
        int nn = n & 0xff;
        return Character.toString(((char) nn));
    }


    public static int val(String x)
    //***************************
    {
        if(trim(x).equals("")){return 0;}
        return Integer.parseInt(trim(x));
    }


    public  static String stuff(String sumber, int mulai, int sebanyak, String diganti)
    {
        String hasil;
        int panjang;
        panjang=sumber.length();
        if(panjang-(mulai+sebanyak-1)==0)
        {
            hasil=left(sumber,mulai-1) + diganti;
        }
        else
        {
            hasil=left(sumber,mulai-1) + diganti + right(sumber,panjang-(mulai+sebanyak-1));
        }
        return hasil;
    }



    public  static String Dec2Bcd(int DecNya, int LenNya)
    //********************************************
    {
        String depan="";
        if (LenNya%2==1){LenNya=LenNya+1;}
        String dHex = String.format("%0"+LenNya+"d", DecNya);
        String RVHex2Txt="";
        int LTxt = len(dHex);
        for( int g = 1;g<=LenNya/2;g++){
            depan = left(dHex,2);
            dHex=stuff(dHex,1,2,"");
            RVHex2Txt=RVHex2Txt+chr(val(left(depan,1))*16+val(right(depan,1)));
        }
        return RVHex2Txt;
    }

    public  static String getTLV(String Tag, String Value, int lenNya )
    {
        String Rv;
        int len  = len(Value);
        if(lenNya==2)
        {
            Rv = Tag+ String.format("%02d", len)+Value;
        }
        else
        {
            Rv = Tag+ String.format("%03d", len)+Value;
        }
        return Rv;
    }







    public  static String Txt2B64(String TextNya)
    {
    // *****************************************
        // encode
        String base64="";
        try {
            byte[] encrpt= TextNya.getBytes("UTF-8");
            base64 = Base64.encodeToString(encrpt, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return base64;
    }




    public  static String B642Txt(String byteNya)
    {
        // *****************************************
        // decode
        byte[] Ktext = Base64.decode(byteNya, Base64.DEFAULT);
        String Rv = new String(Ktext);
        return Rv;
    }


    public static String Byte2Hex(byte [] Txt) {
    // Byte To Hex dengan Txt2Hex Name
        String RVTxt2Hex = "";
        int LTxt = Txt.length;
        int xn, p1, p2,m,k;
        int n;
        byte x;

        for (int g = 0; g < LTxt; g++) {
            x = Txt[g];
            n=x & 0xff;
            if(n<=255)
            {
                p1 = (int) (n / 16) + 1;
                p2 = (int) (n % 16) + 1;
                RVTxt2Hex = RVTxt2Hex + Substr("0123456789ABCDEF", p1, 1) + Substr("0123456789ABCDEF", p2, 1);
            }
            else
            {
                m = (int) n%256;
                k = (int) n/256;
                p1 = (int) (k / 16) + 1;
                p2 = (k % 16) + 1;
                RVTxt2Hex = RVTxt2Hex + Substr("0123456789ABCDEF", p1, 1) + Substr("0123456789ABCDEF", p2, 1);
                p1 = (int) (m / 16) + 1;
                p2 = (m % 16) + 1;
                RVTxt2Hex = RVTxt2Hex + Substr("0123456789ABCDEF", p1, 1) + Substr("0123456789ABCDEF", p2, 1);
            }
//            RVTxt2Hex=RVTxt2Hex+" ";
        }
        return RVTxt2Hex;
    }


    public static String Byte2Hex(byte Txt) {
        // Byte To Hex dengan Txt2Hex Name
        String RVTxt2Hex = "";
        int xn, p1, p2,m,k;
        int n;
        byte x;

            x = Txt;
            xn = (int) x &0xFF;
            p1 = (int) (xn / 16) + 1;
            p2 = (int) (xn % 16) + 1;
            RVTxt2Hex = RVTxt2Hex + Substr("0123456789ABCDEF", p1, 1) + Substr("0123456789ABCDEF", p2, 1);
            return RVTxt2Hex;
    }


    public  static byte [] GetByte2Byte(byte [] tByte, int Mulai, int Sepanjang)
    {
        byte [] Rv= new byte[Sepanjang];
        int K;
        for(K=Mulai;K<Mulai+Sepanjang;K++)
        {
            if(K==tByte.length){break;}
            Rv[K-Mulai]=tByte[K];
        }
        return Rv;
    }


    public  static byte [] CutByte2Byte(byte [] tByte, int Sepanjang)
    {

        byte [] Rv = Arrays.copyOfRange(tByte, Sepanjang, tByte.length);
        return Rv;
/*
        byte [] Rv= new byte[tByte.length-Sepanjang];
        int K,L=0;
        for(K=Sepanjang;K<tByte.length;K++)
        {
            Rv[L]=tByte[K];
            L=L+1;
        }
        return Rv;
 */
    }


    public  static int PosByteOnBytes(byte dicari, byte [] tByte, int Sepanjang)
    {
        int Rv= 0;
        int K;
        for(K=0;K<=Sepanjang;K++)
        {
            if(tByte[K]==dicari){return K;};
        }
        return Rv;
    }

    public  static byte [] Txt2Byte(String tTxt)
    {
        byte [] Rv= new byte [1];
        Rv=Hex2Byte(Txt2Hex(tTxt));
        return Rv;
    }



    public  static String GetByte2Txt(byte [] tByte)
    {
        String Rv="";
        int K;
        for(K=0;K<tByte.length;K++)
        {
            Rv=Rv+ Hex2Txt(Byte2Hex(tByte[K]));
        }
        return Rv;
    }


    public  static String GetByte2Txt(byte [] tByte, int Mulai, int Sepanjang)
    {
        if(Sepanjang>tByte.length){Sepanjang=tByte.length-Mulai;}
        String Rv="";
        int K;
        for(K=Mulai;K<=Mulai+Sepanjang-1;K++)
        {
            Rv=Rv+ Hex2Txt(Byte2Hex(tByte[K]));
        }
        return Rv;
    }


    public  static String GetByte2Txt(byte tByte)
    {
        String Rv="";
        Rv=Rv+ Hex2Txt(Byte2Hex(tByte));
        return Rv;
    }


    public  static String GetByte2Hex(byte [] tByte, int Mulai, int Sepanjang)
    {
        String Rv="";
        int K;
        for(K=Mulai;K<Mulai+Sepanjang;K++)
        {
            Rv=Rv+Byte2Hex(tByte[K]);
        }
        return Rv;
    }


    public  static byte[] Hex2Byte(String tHex)
    {
        int nour=0;
        byte [] RVHex2Txt=new byte[2048];
        int l=tHex.length(),num;
        String dHex="",depan="";
        for (int i=1;i<=l;i++)
        {
            if(!Substr(tHex,i,1).equals(" "))
            {
                dHex=dHex+Substr(tHex,i,1);
            }
        }
        if (dHex.length()%2==1)
        {
            dHex="0"+dHex;
        }
        String x="";
        while (!dHex.equals(""))
        {
            depan = left(dHex,2);
            dHex=stuff(dHex,1,2,"");
            num = Hex2Num(depan);
            RVHex2Txt[nour]=(byte) num;;
            nour = nour+1;
            x=x+""+num+" ";
        }
        byte[] Rval = new byte[nour];
        Rval = GetByte2Byte(RVHex2Txt,0,nour);
        return Rval;
    }


    public   static String DumpByte2Hex(byte [] Txt) {
        // Byte To Hex dengan Txt2Hex Name
        String RVTxt2Hex = "";
        int LTxt = Txt.length;
        int xn, p1, p2,m,k;
        int n;
        byte x;

        for (int g = 0; g < LTxt; g++) {
            x = Txt[g];
            n=x & 0xff;
            if(n<=255)
            {
                p1 = (int) (n / 16) + 1;
                p2 = (int) (n % 16) + 1;
                RVTxt2Hex = RVTxt2Hex + Substr("0123456789ABCDEF", p1, 1) + Substr("0123456789ABCDEF", p2, 1);
            }
            else
            {
                m = (int) n%256;
                k = (int) n/256;
                p1 = (int) (k / 16) + 1;
                p2 = (k % 16) + 1;
                RVTxt2Hex = RVTxt2Hex + Substr("0123456789ABCDEF", p1, 1) + Substr("0123456789ABCDEF", p2, 1);
                p1 = (int) (m / 16) + 1;
                p2 = (m % 16) + 1;
                RVTxt2Hex = RVTxt2Hex + Substr("0123456789ABCDEF", p1, 1) + Substr("0123456789ABCDEF", p2, 1);
            }
            RVTxt2Hex=RVTxt2Hex+" ";
        }
        return RVTxt2Hex;
    }

    public static void hex_dump_char(String pzTitle, byte[] buff, long len)
    {
        int a=0,dump=8;
        byte []  buffdata = new byte[1024];
        byte [][] line = new byte[512][dump];

        try {
            // copy the data to another memory
            System.arraycopy(buff, 0 , buffdata, 0 ,buff.length);

            // print title, len and memory address first
//            printStr("\n\n\n\n\n\n");
            printStr(pzTitle);
            printStr(String.format("Len:%d ", len) + "Addrs: " + buff);

            // if there is no data in the buffer
            // memory is empty
            if (len == 0 || buffdata.length == 0) return;
            if(buff.length != len){
                printStr("Error DUMP len not equals!!!!!!");
                return;}

            int lenTotal = (int)len / dump;
            int sisa = (int)len % dump;

            try {
                for (a = 0; a <= lenTotal; a++) {
                    System.arraycopy(buff, a * dump, line[a], 0, dump);
                    String print="";
                    for(int b=0;b < dump;b++){
                        print = print + com.pax.pay.utils.Fox.Byte2Hex(line[a][b]) + " ";
                    }
                    String dataAllPrint = Component.getPaddedNumber(a,3) + "| " + print + "    " +  com.pax.pay.utils.Fox.GetByte2Txt (line[a]);
                    printStr(dataAllPrint);
                }
            }catch (Exception e){}

            if(sisa > 0) {
                System.arraycopy(buff, lenTotal * dump, line[lenTotal], 0, sisa);
                String print="";
                for(int b=0;b < sisa;b++){
                    print = print + com.pax.pay.utils.Fox.Byte2Hex(line[a][b]) + " ";
                }
                String dataAllPrint = Component.getPaddedNumber(a,3) + "| " + print +Component.getPaddedString("",28 - sisa*2 - sisa, ' ') + com.pax.pay.utils.Fox.GetByte2Txt (line[lenTotal]);
                printStr(dataAllPrint);
            }

            printStr("\n\n\n\n");
        }catch (Exception ex){
            printStr("Error DUMP Brrrrr!!!!!!");
        }
    }

    public static int printStr(String str) {
        IPrinter printer = FinancialApplication.getDal().getPrinter();
        try {
            printer.init();
            printer.setGray(4);
            printer.spaceSet((byte)0,(byte)0);//word,line
            printer.fontSet( EFontTypeAscii.FONT_8_16, EFontTypeExtCode.FONT_16_16);
            printer.printStr(str, null);
            return printer.start();

        } catch (PrinterDevException e) {
            Log.e("printStr kd: ", "", e);
        }

        return -1;
    }

    public static List<String> ParseDataFindStr(String dataIn, String FindChr){
        List<String> Buff = new ArrayList<>();
        String dataParse;
        String buffTemp;
        int lenData;
        Boolean loop = true;

        try {
            if (dataIn == null)
                return Buff;

            while (loop) {
                lenData = dataIn.indexOf(FindChr);
                if (lenData < 0) {
                    loop = false;
                }

                try {
                    if (lenData < 0) {
                        lenData = 0;
                        dataParse = dataIn.substring(lenData);
                    } else {
                        dataParse = dataIn.substring(0, lenData);
                    }
                    Buff.add(dataParse);

                    lenData++;
                    buffTemp = dataIn.substring(lenData);
                    dataIn = buffTemp;
                } catch (Exception ex) {
                    Log.i("Err : ", "ERR PARSE ParseDataFindStr!!!!!!!!!!!!!!!!");
                }
            }
        }catch (Exception ex){
            Log.i("Err : ", "ERR PARSE ParseDataFindStr!!!!!!!!!!!!!!!!");
        }
        return Buff;
    }



    public static int lenTag5 = 5;
    public static int lenTag2 = 2;

    private static String[] TAG = {
            "62",
            "05",
            "NP",
            "91",
            "9F"
    };

    public static HashMap<String,String> TAG_Value  = new HashMap<>();

    public static HashMap<String,String> ParseTLV(int lenTAG, String dataIn){
        boolean loop = true;
        int lenData;
        String buff = dataIn;
        String buffTemp, value;
        String Tag;
        byte lenBuf[];

        try {
            if(dataIn.length() == 0)
                return TAG_Value;

            TAG_Value.clear();

            while (loop) {
                if(buff == null)
                    return TAG_Value;

                Tag = buff.substring(0, 2);
                for(int cntTag=0; cntTag < TAG.length; cntTag ++){
                    if(Tag.equals(TAG[cntTag])){
                        if(lenTAG == lenTag2) {
                            lenBuf = com.pax.pay.utils.Fox.Hex2Byte(buff.substring(2, lenTAG + lenTAG));
                            lenData = (int)lenBuf[0];
                            value = buff.substring(lenTAG + 2, lenTAG + lenData + lenTAG );
                        }else{
                            lenData = Integer.valueOf(buff.substring(2, lenTAG));
                            value = buff.substring(lenTAG, lenTAG + lenData);
                        }
                        String exisData = TAG_Value.get(Tag);
                        if(exisData != null){
                            //sementara cuman handle kl ada TAG sama gw tambahin jadi TAG01, normal nya c gak ada yg sama, kl di AJ ada yg sama
                            int cnt=1;
                            while(true){
                                String exisBuff = TAG_Value.get(Tag + Component.getPaddedNumber(cnt,2));
                                if(exisBuff == null) {
                                    TAG_Value.put(Tag + Component.getPaddedNumber(cnt,2), value);
                                    break;
                                }
                                cnt++;
                            }
                        }else{
                            TAG_Value.put(Tag, value);
                        }

                        lenData = buff.length();
                        if(value.length() == 0) {
                            buffTemp = buff.substring(lenTAG + TAG.length, lenData);
                        }else {
                            if(lenTAG == lenTag2)
                                buffTemp = buff.substring(lenTAG + lenTAG + value.length(), lenData);
                            else
                                buffTemp = buff.substring(lenTAG + value.length(), lenData);
                        }
                        buff = buffTemp.substring(0, buffTemp.length());
                    }else{//kalau gak ada yg sama dari tag list
                        if((lenTAG == lenTag2) &&(cntTag == TAG.length - 1) ){
                            return TAG_Value;
                        }
                    }
                }
            }
        }catch(Exception ex){
            Log.i("Err ParseTLV", ex.getMessage());
        }

        return TAG_Value;
    }

    public static void emvPrintTAG(IEmv emv, TransData transData){

        printStr("emvPrintTAG\n");
        byte[]tag0x9F33 = emv.getTlv(0x9F33);
        printStr("TERM CAP : " + Utils.bcd2Str(tag0x9F33) + "\n");
        byte[]tag0x9F40 = emv.getTlv(0x9F40);
        printStr("ADD TERM CAP : " + Utils.bcd2Str(tag0x9F40) + "\n");
        byte[]tag0x9F12 = emv.getTlv(0x9F12);
        printStr("Txt : " + Utils.bcd2Str(tag0x9F12) + "\n");

        printStr("APPL ID   : " + transData.getAid() + "\n");

        byte[]tag0x8F = emv.getTlv(0x8F);
        printStr("Cert AuthPublic Key Index : " + Utils.bcd2Str(tag0x8F) + "\n\n");

        byte[]tag0x5A = emv.getTlv(0x5A);
        printStr("PAN : " + Utils.bcd2Str(tag0x5A) + "\n");

        byte[]tag0x9A = emv.getTlv(0x9A);
        printStr("Transaction Date : " + Utils.bcd2Str(tag0x9A) + "\n");

        byte[]tag0x9C = emv.getTlv(0x9C);
        printStr("Transaction Type : " + Utils.bcd2Str(tag0x9C) + "\n");

        byte[]tag0x5F2A = emv.getTlv(0x5F2A);
        printStr("Trans Curr Code : " + Utils.bcd2Str(tag0x5F2A) + "\n");

        byte[]t0x9F1A = emv.getTlv(0x9F1A);
        printStr("Term Country Code : " + Utils.bcd2Str(t0x9F1A) + "\n");

        byte[]t0x9F02 = emv.getTlv(0x9F02);
        if (t0x9F02 != null) {
            printStr("Amount Authorized : " + Utils.bcd2Str(t0x9F02) + "\n");
        }

        byte[]t0x9F03 = emv.getTlv(0x9F03);
        if (t0x9F03 != null) {
            printStr("Amount other: " + Utils.bcd2Str(t0x9F03) + "\n");
        }

        byte[]t0x5F34 = emv.getTlv(0x5F34);
        if (t0x5F34 !=null) {
            printStr("PAN Sequence No : " + Utils.bcd2Str(t0x5F34) + "\n\n");
        }

        printStr("Terminal Action Codes \n");

        printStr("DEN: " + Utils.bcd2Str(aidParam.getTacDenial())+ "\n");

        printStr("ONL: " + Utils.bcd2Str(aidParam.getTacOnline())+ "\n");

        printStr("DEF: " + Utils.bcd2Str(aidParam.getTacDefault())+ "\n\n");

        printStr("Issuer Action Codes " + transData.getTvr()+"\n");

        byte[]t0x9F0E = emv.getTlv(0x9F0E);
        if (t0x5F34 !=null) {
            printStr("DEN : " + Utils.bcd2Str(t0x9F0E) + "\n\n");
        }

        byte[]t0x9F0F = emv.getTlv(0x9F0F);
        if (t0x5F34 !=null) {
            printStr("ONL : " + Utils.bcd2Str(t0x9F0F) + "\n\n");
        }

        byte[]t0x9F0D = emv.getTlv(0x9F0D);
        if (t0x5F34 !=null) {
            printStr("DEF : " + Utils.bcd2Str(t0x9F0D) + "\n\n");
        }

        byte[]t0x82 = emv.getTlv(0x82);
        printStr("AIP : " + Utils.bcd2Str(t0x82)+"\n\n");

        printStr("TVR VALUE : " + transData.getTvr()+"\n");

        byte[]t0xDF04 = emv.getTlv(0xDF04);
        printStr("CVMR : " + Utils.bcd2Str(t0xDF04)+"\n");

        byte[]t0x9B = emv.getTlv(0x9B);
        printStr("TSI : " + Utils.bcd2Str(t0x9B)+"\n");

        byte[]t0x9F10 = emv.getTlv(0x9F10);
        printStr("Issuer app data : " + Utils.bcd2Str(t0x9F10)+"\n");

        byte[]t0x8A = emv.getTlv(0x8A);
        printStr("Auth Response Code : " + Utils.bcd2Str(t0x8A)+"\n\n");

        byte[]t0x9F53 = emv.getTlv(0x9F53);
        printStr("Scheme Specific Tags" +"\n");
        printStr("9F53 : " + Utils.bcd2Str(t0x9F53)+"\n\n");

        printStr("GEN AC CID" +"\n");
//        printStr("Req: " +"\n");

        byte[]t0x9F26 = emv.getTlv(0x9F26);
        printStr("AC : " + Utils.bcd2Str(t0x9F26)+"\n");

        byte[]t0x9F36 = emv.getTlv(0x9F36);
        printStr("ATC : " + Utils.bcd2Str(t0x9F36)+"\n");

        byte[]t0x9F37 = emv.getTlv(0x9F37);
        printStr("Unpredictable Num : " + Utils.bcd2Str(t0x9F37)+"\n\n");

        printStr("Amount : " + transData.getAmount()+"00"+"\n");

        printStr("Prev. Amount : 00000000\n");

        printStr("Total Amount : " + transData.getAmount()+"00"+"\n");

        byte[]t0x9F1B = emv.getTlv(0x9F1B);
        printStr("Floor : " + Utils.bcd2Str(t0x9F1B)+"\n");
    }

    public static void updateDate(String tgl, Context context) {
        try {
            int year, month, date, hour, minute, second;
            String temp;
            temp = tgl.substring(0, 4);
            year = Integer.valueOf(temp); Log.i("abdul", "tahun : " + year);
            temp = tgl.substring(4, 6);
            month = Integer.valueOf(temp); Log.i("abdul", "month : " + month);
            temp = tgl.substring(6, 8);
            date = Integer.valueOf(temp); Log.i("abdul", "date : " + date);
            temp = tgl.substring(8, 10);
            hour = Integer.valueOf(temp); Log.i("abdul", "hour : " + hour);
            temp = tgl.substring(10, 12);
            minute = Integer.valueOf(temp); Log.i("abdul", "minute : " + minute);
            temp = tgl.substring(12, 14);
            second = Integer.valueOf(temp); Log.i("abdul", "second : " + second);
            Calendar c = Calendar.getInstance();
            Log.i("abdul", "tgl yang di update : " + year + month + date + hour + minute + second);
            /*c.set(year, month, date, hour, minute, second);*/
            c.set(year, month-1, date, hour, minute, second); // ubah 2 jan 2019 bulan nya yang ke update lebih 1
            AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            am.setTime(c.getTimeInMillis());
            Log.i("abdul", "update time berhasil");
        } catch (Exception e) {
            Log.i("abdul", "update time gagal");
            e.printStackTrace();
        }
    }

    public static String ChangeFormatAmt(String[] Amount){
        int len,Blen =3;
        String DataOut;
        String[] change = new String[10];
        for(len =0;len<=4;len++)
        {
            change[len] = Amount[Blen];
            Blen --;
        }
        DataOut  = change[4];
        return DataOut;
//        memcpy(DataOut, change, 4);
    }


}
