USE master;
GO

-- =======================================================
-- 1. TẠO DATABASE VÀ DỌN DẸP DỮ LIỆU CŨ
-- =======================================================
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'Nhat_Tien_Thanh_Kho')
BEGIN
    ALTER DATABASE Nhat_Tien_Thanh_Kho SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE Nhat_Tien_Thanh_Kho;
END
GO

CREATE DATABASE Nhat_Tien_Thanh_Kho;
GO

USE Nhat_Tien_Thanh_Kho;
GO

-- =======================================================
-- 2. TẠO CẤU TRÚC BẢNG (SCHEMA)
-- =======================================================

-- 1. Hãng Sản Xuất
CREATE TABLE DMHangSX (
    MaHang INT IDENTITY(1,1) NOT NULL,
    TenHang NVARCHAR(255),
    CONSTRAINT PK_DMHangSX PRIMARY KEY (MaHang)
);

-- 2. Loại Sản Phẩm
CREATE TABLE LoaiSP (
    MaLoai INT IDENTITY(1,1) NOT NULL, 
    TenLoai NVARCHAR(255),
    CONSTRAINT PK_LoaiSP PRIMARY KEY (MaLoai)
);

-- 3. Kho
CREATE TABLE Kho (
    MaKho INT IDENTITY(1,1) NOT NULL,
    TenKho NVARCHAR(255),
    DiaChi NVARCHAR(MAX),
    CONSTRAINT PK_Kho PRIMARY KEY (MaKho)
);

-- 4. Trạng Thái
CREATE TABLE TrangThai (
    MaTrangThai INT NOT NULL,
    TenTrangThai NVARCHAR(100),
    CONSTRAINT PK_TrangThai PRIMARY KEY (MaTrangThai)
);

-- 5. Loại Đối Tác (NCC/Khách hàng)
CREATE TABLE LoaiDonVi (
    LoaiDonVi INT NOT NULL,
    TenLoai NVARCHAR(255),
    CONSTRAINT PK_LoaiDonVi PRIMARY KEY (LoaiDonVi)
);

-- 6. Vai Trò (Admin/Staff)
CREATE TABLE Vaitro (
    MaVaitro INT NOT NULL,
    TenVaiTro NVARCHAR(100),
    CONSTRAINT PK_Vaitro PRIMARY KEY (MaVaitro)
);

-- 7. Danh Mục Sản Phẩm
CREATE TABLE DMSanPham (
    MaSP VARCHAR(50) NOT NULL,
    TenSP NVARCHAR(255),
    Donvitinh NVARCHAR(50),
    Mota NVARCHAR(MAX),
    MaHang INT,
    MaLoai INT, 
    SoLuong INT DEFAULT 0, -- Số lượng tổng (Snapshot)
    
    CONSTRAINT PK_DMSanPham PRIMARY KEY (MaSP),
    CONSTRAINT FK_DMSanPham_DMHangSX FOREIGN KEY (MaHang) REFERENCES DMHangSX(MaHang),
    CONSTRAINT FK_DMSanPham_LoaiSP FOREIGN KEY (MaLoai) REFERENCES LoaiSP(MaLoai)
);

-- 8. Danh Mục Đơn Vị (NCC/KH)
CREATE TABLE DMDonVi (
    MaDonVi VARCHAR(50) NOT NULL,
    MaCode INT IDENTITY(1,1), -- Tự tăng để quản lý nội bộ nếu cần
    TenDonVi NVARCHAR(255),
    LoaiDonVi INT,
    MaSoThue VARCHAR(50),
    SoDienThoai VARCHAR(20),
    Email VARCHAR(100),
    DiaChi NVARCHAR(MAX),
    
    CONSTRAINT PK_DMDonVi PRIMARY KEY (MaDonVi),
    CONSTRAINT FK_DMDonVi_LoaiDonVi FOREIGN KEY (LoaiDonVi) REFERENCES LoaiDonVi(LoaiDonVi)
);

-- 9. Tài Khoản
CREATE TABLE TaiKhoan (
    MaTaiKhoan INT IDENTITY(1,1) NOT NULL,
    TenTaiKhoan NVARCHAR(100) NOT NULL,
    Password VARCHAR(255) NOT NULL, 
    MaVaitro INT,
    MaKho INT,
    
    CONSTRAINT PK_TaiKhoan PRIMARY KEY (MaTaiKhoan),
    CONSTRAINT FK_TaiKhoan_Vaitro FOREIGN KEY (MaVaitro) REFERENCES Vaitro(MaVaitro),
    CONSTRAINT FK_TaiKhoan_Kho FOREIGN KEY (MaKho) REFERENCES Kho(MaKho)
);

-- 10. Phiếu Nhập
CREATE TABLE PhieuNhap (
    SoPhieu VARCHAR(50) NOT NULL,
    MaKho INT,
    MaDonVi VARCHAR(50), 
    NgayNhap DATETIME DEFAULT GETDATE(),
    VAT DECIMAL(19, 2) DEFAULT 0,
    SoLuong INT DEFAULT 0,
    TongTien DECIMAL(19, 2) DEFAULT 0,
    GhiChu NVARCHAR(MAX),
    
    CONSTRAINT PK_PhieuNhap PRIMARY KEY (SoPhieu),
    CONSTRAINT FK_PhieuNhap_Kho FOREIGN KEY (MaKho) REFERENCES Kho(MaKho),
    CONSTRAINT FK_PhieuNhap_DMDonVi FOREIGN KEY (MaDonVi) REFERENCES DMDonVi(MaDonVi)
);

-- 11. Phiếu Xuất
CREATE TABLE PhieuXuat (
    SoPhieu VARCHAR(50) NOT NULL,
    MaKho INT,
    MaDonVi VARCHAR(50),
    NgayXuat DATETIME DEFAULT GETDATE(),
    VAT DECIMAL(19, 2) DEFAULT 0,
    SoLuong INT DEFAULT 0,
    TongTien DECIMAL(19, 2) DEFAULT 0,
    GhiChu NVARCHAR(MAX),
    
    CONSTRAINT PK_PhieuXuat PRIMARY KEY (SoPhieu),
    CONSTRAINT FK_PhieuXuat_Kho FOREIGN KEY (MaKho) REFERENCES Kho(MaKho),
    CONSTRAINT FK_PhieuXuat_DMDonVi FOREIGN KEY (MaDonVi) REFERENCES DMDonVi(MaDonVi)
);

-- =======================================================
-- ĐÃ DỜI LÊN TRÊN: Tạo bảng DMMay trước khi tạo Chi tiết phiếu
-- =======================================================
-- 12. Danh Mục Máy (Serial)
CREATE TABLE DMMay (
    MaMay VARCHAR(50) NOT NULL,
    MaSP VARCHAR(50) NOT NULL,
    MaKho INT,
    SoSeri VARCHAR(100) NULL, 
    NgayTao DATETIME DEFAULT GETDATE(),
    MaTrangThai INT,
    SoPhieuNhap VARCHAR(50),
    TonKho BIT DEFAULT 1, -- 1: Đang trong kho, 0: Đã xuất
    
    CONSTRAINT PK_DMMay PRIMARY KEY (MaMay),
    CONSTRAINT UK_DMMay_SoSeri UNIQUE (SoSeri), 
    CONSTRAINT FK_DMMay_DMSanPham FOREIGN KEY (MaSP) REFERENCES DMSanPham(MaSP),
    CONSTRAINT FK_DMMay_Kho FOREIGN KEY (MaKho) REFERENCES Kho(MaKho),
    CONSTRAINT FK_DMMay_TrangThai FOREIGN KEY (MaTrangThai) REFERENCES TrangThai(MaTrangThai),
    CONSTRAINT FK_DMMay_PhieuNhap FOREIGN KEY (SoPhieuNhap) REFERENCES PhieuNhap(SoPhieu)
);

-- =======================================================
-- TẠO CÁC BẢNG CHI TIẾT SAU KHI ĐÃ CÓ BẢNG DMMAY
-- =======================================================

-- 13. Chi Tiết Phiếu Nhập
CREATE TABLE CTPhieuNhap (
    MaCTPN INT IDENTITY(1,1) NOT NULL,
    SoPhieu VARCHAR(50) NOT NULL,
    MaSP VARCHAR(50) NOT NULL,
    MaMay VARCHAR(50) NOT NULL, 
    DonGia DECIMAL(19, 2) DEFAULT 0,
    GhiChu NVARCHAR(MAX),
    NgayTao DATETIME DEFAULT GETDATE(),

    CONSTRAINT PK_CTPhieuNhap PRIMARY KEY (MaCTPN),
    CONSTRAINT FK_CTPhieuNhap_PhieuNhap FOREIGN KEY (SoPhieu) REFERENCES PhieuNhap(SoPhieu),
    CONSTRAINT FK_CTPhieuNhap_DMSanPham FOREIGN KEY (MaSP) REFERENCES DMSanPham(MaSP),
    CONSTRAINT FK_CTPhieuNhap_DMMay FOREIGN KEY (MaMay) REFERENCES DMMay(MaMay),
    
    -- [KHÓA BẤT TỬ 1]: Chống nhân bản máy in trong cùng hoặc khác phiếu nhập
    CONSTRAINT UQ_CTPhieuNhap_MaMay UNIQUE (MaMay) 
);

-- 14. Chi Tiết Phiếu Xuất
CREATE TABLE CTPhieuXuat (
    MaCTPX INT IDENTITY(1,1) NOT NULL,
    SoPhieu VARCHAR(50) NOT NULL,
    MaSP VARCHAR(50) NOT NULL,
    MaMay VARCHAR(50) NOT NULL, 
    DonGia DECIMAL(19, 2) DEFAULT 0,
    GhiChu NVARCHAR(MAX),
    NgayTao DATETIME DEFAULT GETDATE(),

    CONSTRAINT PK_CTPhieuXuat PRIMARY KEY (MaCTPX),
    CONSTRAINT FK_CTPhieuXuat_PhieuXuat FOREIGN KEY (SoPhieu) REFERENCES PhieuXuat(SoPhieu),
    CONSTRAINT FK_CTPhieuXuat_DMSanPham FOREIGN KEY (MaSP) REFERENCES DMSanPham(MaSP),
    CONSTRAINT FK_CTPhieuXuat_DMMay FOREIGN KEY (MaMay) REFERENCES DMMay(MaMay),
    
    -- [KHÓA BẤT TỬ 2]: Chống xuất 1 máy nhiều lần cho các phiếu khác nhau
    CONSTRAINT UQ_CTPhieuXuat_MaMay UNIQUE (MaMay)
);
GO

-- 15. Tồn Kho (Lịch sử chốt sổ) - Đã gom gọn các cột
CREATE TABLE DMTonKho (
    Nam INT NOT NULL,
    MaKho INT NOT NULL,
    MaSP VARCHAR(50) NOT NULL,
    MaTrangThai INT NOT NULL,
    
    SoLuong INT DEFAULT 0,           -- Tồn đầu năm
    GiaTri DECIMAL(19, 2) DEFAULT 0, -- Giá trị đầu năm
    
    CONSTRAINT PK_DMTonKho PRIMARY KEY (Nam, MaSP, MaKho, MaTrangThai), 
    CONSTRAINT FK_DMTonKho_DMSanPham FOREIGN KEY (MaSP) REFERENCES DMSanPham(MaSP),
    CONSTRAINT FK_DMTonKho_Kho FOREIGN KEY (MaKho) REFERENCES Kho(MaKho),
    CONSTRAINT FK_DMTonKho_TrangThai FOREIGN KEY (MaTrangThai) REFERENCES TrangThai(MaTrangThai)
);