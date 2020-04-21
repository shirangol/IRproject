package Model;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumConverter {
    /**
     * Converts a term into a numeric format
     * @param toConvert
     * @return
     */
    public String convert(String toConvert) {

        String str= getNum(toConvert);
        double val = Double.valueOf(str);
        if(val<1000){
            str= getRoundNumber(str);
            StringTokenizer sFraction = new StringTokenizer(toConvert, " ");
            String s= " ";
            if(sFraction.hasMoreTokens()){
                s= sFraction.nextToken();
            } if(sFraction.hasMoreTokens()){
                s= sFraction.nextToken();
            }
            if(s.contains("/")){
                return str + " " +s;
            }
            else if(s.equals("Million")){
                return str + "M";
            }
            else if(s.equals("Billion")){
                return str + "B";
            }
            else if(s.equals("Thousand")){
                return str + "K";
            }
            return str;
        }
        //K
        else if(val>=1000 && val<1000000){
            str= getRoundNumber(String.valueOf(val/1000));
            return str+"K";
        }
        //M
        else if (val>=1000000 && val<Math.pow(10,9)){
            val= val/(Math.pow(10,6));
            str= getRoundNumber((String.valueOf(val)));
            return str+"M";
        }
        //B
        else{
            val= val/(Math.pow(10,9));
            str= getRoundNumber((String.valueOf(val)));
            return str+"B";
        }
    }

    /**
     * Receives a string and returns a string contains the raw number
     * @param s
     * @return
     */
    private String getNum(String s){
        String str ="";
        Pattern p = Pattern.compile("(\\d+)?" + "?\\d+(\\.\\d+)?");
        Matcher m = p.matcher( s );
        while (m.find()){
            str += m.group();
        }

        return str;
    }

    /**
     * Rounds the number, returns it as a string
     * @param str
     * @return
     */
    private String getRoundNumber (String str){
       if(str.contains(".")){
            for (int i= 0; i<str.length();i++){
                if(str.charAt(i)== '.'){
                    if (str.length()- i>3){
                        str= str.substring(0,i+4);
                    }
                 for (int j=str.length()-1; j>i;j--){
                     if(str.charAt(j)!='0'){
                         return str.substring(0,j+1);
                     }
                 }
                 str= str.substring(0,i);
                }
            }
        }
        return str;
    }
}
