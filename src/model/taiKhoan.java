package model;
public class taiKhoan {
    public static String taiKhoan;
    private String matKhau;
    public taiKhoan() {
    }

    public taiKhoan(String taiKhoan, String matKhau) {
        this.taiKhoan = taiKhoan;
        this.matKhau = matKhau;
    }

    public String getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(String taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }


    @Override
    public String toString() {
        return "taiKhoan{" + "taiKhoan=" + taiKhoan + ", matKhau=" + matKhau + '}';
    }
    
}
