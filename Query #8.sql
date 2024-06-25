
CREATE DATABASE qlthuvien;

USE qlthuvien;



CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT,
    message TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES Users(id)
);
CREATE TABLE Sach (
    maSach INT ,
    tenSach VARCHAR(255) NOT NULL,
    nguoiMuon VARCHAR(255),
    ngayMuon DATE,
    ngayHenTra DATE
);

CREATE TABLE TaiKhoan (
    taikhoan VARCHAR(50) ,
    matkhau VARCHAR(255) NOT NULL
);