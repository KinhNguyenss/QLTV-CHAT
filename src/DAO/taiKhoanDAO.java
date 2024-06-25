/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import model.taiKhoan;
import java.security.MessageDigest;
import java.util.Base64;
/**
 *
 * @author kinhn
 */
public class taiKhoanDAO {
     public static boolean taiKhoanDangNhap(taiKhoan tk) {
        Connection conn = connect.connectDb.getconnect();
        String sql = "select * from taikhoan where taikhoan = ? and matkhau = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, tk.getTaiKhoan());
            pst.setString(2, tk.getMatKhau());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connect.connectDb.closeConnect(conn);
        }
        return false;
    }
          public static boolean taiKhoan(taiKhoan tk) {
        Connection conn = connect.connectDb.getconnect();
        String sql = "select matkhau from taikhoan where taikhoan = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, tk.getTaiKhoan());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connect.connectDb.closeConnect(conn);
        }
        return false;
    }
     public static boolean dangKy (taiKhoan tk){
          Connection conn = connect.connectDb.getconnect();
        String sql = "insert into taikhoan (taikhoan,matkhau)" + "values(?,?)";
         try {
              PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, tk.getTaiKhoan());
            pst.setString(2, tk.getMatKhau());
            pst.executeUpdate();
            return true;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return false;
     }
     public static String maHoa (String pass){
         try {
             MessageDigest md = MessageDigest.getInstance("SHA");
             md.update(pass.getBytes());
             byte[] rbt = md.digest();
             StringBuilder sb = new StringBuilder();
             
             for(byte b : rbt){
                 sb.append(String.format("%02x", b));
             }
             
             return sb.toString();
         } catch (Exception e) {
         }
         return null;
     }
}
