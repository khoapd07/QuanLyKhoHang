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




-- 14. Tồn Kho (Lịch sử chốt sổ)
CREATE TABLE DMTonKho (
    Nam INT NOT NULL,
    MaSP VARCHAR(50) NOT NULL,
    MaKho INT NOT NULL,
    SoLuongDau INT DEFAULT 0,
    GiaTriDau DECIMAL(19, 2) DEFAULT 0,
    MaTrangThai INT NOT NULL,
    
    CONSTRAINT PK_DMTonKho PRIMARY KEY (Nam, MaSP, MaKho, MaTrangThai), 
    CONSTRAINT FK_DMTonKho_DMSanPham FOREIGN KEY (MaSP) REFERENCES DMSanPham(MaSP),
    CONSTRAINT FK_DMTonKho_Kho FOREIGN KEY (MaKho) REFERENCES Kho(MaKho),
    CONSTRAINT FK_DMTonKho_TrangThai FOREIGN KEY (MaTrangThai) REFERENCES TrangThai(MaTrangThai)
);

-- 15. Chi Tiết Phiếu Nhập
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
    CONSTRAINT FK_CTPhieuNhap_DMMay FOREIGN KEY (MaMay) REFERENCES DMMay(MaMay)
);

-- 16. Chi Tiết Phiếu Xuất
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
    CONSTRAINT FK_CTPhieuXuat_DMMay FOREIGN KEY (MaMay) REFERENCES DMMay(MaMay)
);

GO

-- =======================================================
-- 3. STORED PROCEDURES ( thủ tục )
-- =======================================================

USE Nhat_Tien_Thanh_Kho;
GO

-- 1. THỦ TỤC CHỐT SỔ: Mặc định máy không trạng thái là ID 1 (Bình thường)
CREATE OR ALTER PROCEDURE sp_ChotSoTonDauNam
    @NamCanChot INT, -- Ví dụ: 2025
    @MaKho INT       -- 0: Tất cả, hoặc ID cụ thể
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @NamTonDau INT = @NamCanChot + 1; -- Năm mới: 2026
    DECLARE @NgayDauNam DATE = DATEFROMPARTS(@NamCanChot, 1, 1);
    DECLARE @NgayCuoiNam DATETIME = DATETIMEFROMPARTS(@NamCanChot, 12, 31, 23, 59, 59, 999);

    -- 1. Xóa dữ liệu cũ của năm mới (để tránh chốt trùng)
    IF @MaKho = 0
        DELETE FROM DMTonKho WHERE Nam = @NamTonDau;
    ELSE
        DELETE FROM DMTonKho WHERE Nam = @NamTonDau AND MaKho = @MaKho;

    -- 2. Tính toán tồn cuối năm nay để làm tồn đầu năm sau
    ;WITH TonDauNamNay AS (
        -- Lấy số dư đầu kỳ của năm đang chốt (nếu có)
        SELECT MaKho, MaSP, MaTrangThai, SUM(SoLuongDau) AS SL, SUM(GiaTriDau) AS GT
        FROM DMTonKho 
        WHERE Nam = @NamCanChot AND (@MaKho = 0 OR MaKho = @MaKho)
        GROUP BY MaKho, MaSP, MaTrangThai
    ),
    BienDongTrongNam AS (
        -- Nhập trong năm
        SELECT pn.MaKho, sp.MaSP, ISNULL(m.MaTrangThai, 1) AS MaTrangThai, 
               COUNT(*) AS SL_Nhap, SUM(ctn.DonGia) AS GT_Nhap,
               0 AS SL_Xuat, 0 AS GT_Xuat
        FROM CTPhieuNhap ctn
        JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu
        JOIN DMMay m ON ctn.MaMay = m.MaMay
        JOIN DMSanPham sp ON m.MaSP = sp.MaSP
        WHERE pn.NgayNhap BETWEEN @NgayDauNam AND @NgayCuoiNam
          AND (@MaKho = 0 OR pn.MaKho = @MaKho)
        GROUP BY pn.MaKho, sp.MaSP, m.MaTrangThai

        UNION ALL

        -- Xuất trong năm
        SELECT px.MaKho, sp.MaSP, ISNULL(m.MaTrangThai, 1) AS MaTrangThai,
               0, 0,
               COUNT(*) AS SL_Xuat, SUM(ctx.DonGia) AS GT_Xuat
        FROM CTPhieuXuat ctx
        JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu
        JOIN DMMay m ON ctx.MaMay = m.MaMay
        JOIN DMSanPham sp ON m.MaSP = sp.MaSP
        WHERE px.NgayXuat BETWEEN @NgayDauNam AND @NgayCuoiNam
          AND (@MaKho = 0 OR px.MaKho = @MaKho)
        GROUP BY px.MaKho, sp.MaSP, m.MaTrangThai
    ),
    TongKet AS (
        SELECT 
            COALESCE(td.MaKho, bd.MaKho) AS MaKho,
            COALESCE(td.MaSP, bd.MaSP) AS MaSP,
            COALESCE(td.MaTrangThai, bd.MaTrangThai) AS MaTrangThai,
            (ISNULL(MAX(td.SL), 0) + SUM(ISNULL(bd.SL_Nhap, 0)) - SUM(ISNULL(bd.SL_Xuat, 0))) AS TonCuoi_SL,
            (ISNULL(MAX(td.GT), 0) + SUM(ISNULL(bd.GT_Nhap, 0)) - SUM(ISNULL(bd.GT_Xuat, 0))) AS TonCuoi_GT
        FROM TonDauNamNay td
        FULL OUTER JOIN BienDongTrongNam bd 
            ON td.MaKho = bd.MaKho AND td.MaSP = bd.MaSP AND td.MaTrangThai = bd.MaTrangThai
        GROUP BY COALESCE(td.MaKho, bd.MaKho), COALESCE(td.MaSP, bd.MaSP), COALESCE(td.MaTrangThai, bd.MaTrangThai)
    )
    -- 3. Lưu vào bảng DMTonKho cho năm sau
    INSERT INTO DMTonKho (Nam, MaKho, MaSP, MaTrangThai, SoLuongDau, GiaTriDau)
    SELECT @NamTonDau, MaKho, MaSP, MaTrangThai, TonCuoi_SL, TonCuoi_GT
    FROM TongKet
    WHERE TonCuoi_SL > 0; -- Chỉ lưu những gì còn tồn
END;
GO
-- 1. THỦ TỤC LẤY DỮ LIỆU (PHÂN TRANG)
-- =============================================
CREATE OR ALTER PROCEDURE sp_BaoCaoXuatNhapTon_PhanTrang
    @MaKho INT,
    @TuNgay DATE,
    @DenNgay DATETIME, 
    @LoaiLoc INT,
    @PageNumber INT, 
    @PageSize INT    
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @NamBaoCao INT = YEAR(@TuNgay);
    DECLARE @NgayDauNam DATE = DATEFROMPARTS(@NamBaoCao, 1, 1);

    -- Lấy Tồn Đầu từ bảng chốt
    DECLARE @TonDauNam TABLE (MaSP VARCHAR(50), MaTrangThai INT, SoLuong INT, GiaTri DECIMAL(19,2));
    INSERT INTO @TonDauNam
    SELECT MaSP, MaTrangThai, SUM(SoLuongDau), SUM(GiaTriDau)
    FROM DMTonKho WHERE Nam = @NamBaoCao AND (@MaKho = 0 OR MaKho = @MaKho)
    GROUP BY MaSP, MaTrangThai;

    -- Tính biến động
    WITH BienDong AS (
        -- Nhập
        SELECT sp.MaSP, ISNULL(m.MaTrangThai, 0) AS MaTrangThai,
            SUM(CASE WHEN pn.NgayNhap >= @NgayDauNam AND pn.NgayNhap < @TuNgay THEN 1 ELSE 0 END) AS NhapDem_SL,
            SUM(CASE WHEN pn.NgayNhap >= @NgayDauNam AND pn.NgayNhap < @TuNgay THEN ctn.DonGia ELSE 0 END) AS NhapDem_GT,
            SUM(CASE WHEN pn.NgayNhap >= @TuNgay AND pn.NgayNhap <= @DenNgay THEN 1 ELSE 0 END) AS NhapTrong_SL,
            SUM(CASE WHEN pn.NgayNhap >= @TuNgay AND pn.NgayNhap <= @DenNgay THEN ctn.DonGia ELSE 0 END) AS NhapTrong_GT,
            0 AS XuatDem_SL, 0 AS XuatDem_GT, 0 AS XuatTrong_SL, 0 AS XuatTrong_GT
        FROM CTPhieuNhap ctn JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu 
        JOIN DMMay m ON ctn.MaMay = m.MaMay JOIN DMSanPham sp ON m.MaSP = sp.MaSP
        WHERE (@MaKho = 0 OR pn.MaKho = @MaKho) AND pn.NgayNhap >= @NgayDauNam 
        GROUP BY sp.MaSP, ISNULL(m.MaTrangThai, 0)
        
        UNION ALL
        
        -- Xuất
        SELECT sp.MaSP, ISNULL(m.MaTrangThai, 0) AS MaTrangThai,
            0, 0, 0, 0,
            SUM(CASE WHEN px.NgayXuat >= @NgayDauNam AND px.NgayXuat < @TuNgay THEN 1 ELSE 0 END) AS XuatDem_SL,
            SUM(CASE WHEN px.NgayXuat >= @NgayDauNam AND px.NgayXuat < @TuNgay THEN ctx.DonGia ELSE 0 END) AS XuatDem_GT,
            SUM(CASE WHEN px.NgayXuat >= @TuNgay AND px.NgayXuat <= @DenNgay THEN 1 ELSE 0 END) AS XuatTrong_SL,
            SUM(CASE WHEN px.NgayXuat >= @TuNgay AND px.NgayXuat <= @DenNgay THEN ctx.DonGia ELSE 0 END) AS XuatTrong_GT
        FROM CTPhieuXuat ctx JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu 
        JOIN DMMay m ON ctx.MaMay = m.MaMay JOIN DMSanPham sp ON m.MaSP = sp.MaSP
        WHERE (@MaKho = 0 OR px.MaKho = @MaKho) AND px.NgayXuat >= @NgayDauNam 
        GROUP BY sp.MaSP, ISNULL(m.MaTrangThai, 0)
    )
    SELECT 
        sp.MaSP, sp.TenSP, 
        COALESCE(bd.MaTrangThai, tdn.MaTrangThai, 0) AS MaTrangThai, 
        ISNULL(tt.TenTrangThai, N'') AS TenTrangThai,
        sp.Donvitinh, 
        (ISNULL(tdn.SoLuong, 0) + SUM(ISNULL(bd.NhapDem_SL, 0)) - SUM(ISNULL(bd.XuatDem_SL, 0))) AS TonDau, 
        SUM(ISNULL(bd.NhapTrong_SL, 0)) AS NhapTrong, 
        SUM(ISNULL(bd.XuatTrong_SL, 0)) AS XuatTrong, 
        ((ISNULL(tdn.SoLuong, 0) + SUM(ISNULL(bd.NhapDem_SL, 0)) - SUM(ISNULL(bd.XuatDem_SL, 0))) + SUM(ISNULL(bd.NhapTrong_SL, 0)) - SUM(ISNULL(bd.XuatTrong_SL, 0))) AS TonCuoi, 
        ((ISNULL(tdn.GiaTri, 0) + SUM(ISNULL(bd.NhapDem_GT, 0)) - SUM(ISNULL(bd.XuatDem_GT, 0))) + SUM(ISNULL(bd.NhapTrong_GT, 0)) - SUM(ISNULL(bd.XuatTrong_GT, 0))) AS ThanhTien
    FROM DMSanPham sp
    LEFT JOIN (SELECT MaSP, MaTrangThai FROM BienDong UNION SELECT MaSP, MaTrangThai FROM @TonDauNam) AS Keys ON sp.MaSP = Keys.MaSP
    LEFT JOIN BienDong bd ON Keys.MaSP = bd.MaSP AND Keys.MaTrangThai = bd.MaTrangThai
    LEFT JOIN @TonDauNam tdn ON Keys.MaSP = tdn.MaSP AND Keys.MaTrangThai = tdn.MaTrangThai
    LEFT JOIN TrangThai tt ON Keys.MaTrangThai = tt.MaTrangThai
    WHERE (@MaKho = 0 OR EXISTS (SELECT 1 FROM DMTonKho WHERE MaKho = @MaKho))
    GROUP BY sp.MaSP, sp.TenSP, sp.Donvitinh, tdn.SoLuong, tdn.GiaTri, COALESCE(bd.MaTrangThai, tdn.MaTrangThai, 0), tt.TenTrangThai
    HAVING (ISNULL(tdn.SoLuong, 0) + SUM(ISNULL(bd.NhapDem_SL, 0)) - SUM(ISNULL(bd.XuatDem_SL, 0))) > 0 
        OR SUM(bd.NhapTrong_SL) > 0 
        OR SUM(bd.XuatTrong_SL) > 0
    ORDER BY sp.MaSP, MaTrangThai
    OFFSET (@PageNumber * @PageSize) ROWS
    FETCH NEXT @PageSize ROWS ONLY;
END;
GO

-- =============================================
-- 2. THỦ TỤC ĐẾM TỔNG SỐ DÒNG (FIX LỖI GROUP BY)
-- =============================================

CREATE OR ALTER PROCEDURE sp_Count_BaoCaoXuatNhapTon
    @MaKho INT, @TuNgay DATE, @DenNgay DATETIME, @LoaiLoc INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @NamBaoCao INT = YEAR(@TuNgay);
    DECLARE @NgayDauNam DATE = DATEFROMPARTS(@NamBaoCao, 1, 1);

    -- 1. Tồn đầu năm
    DECLARE @TonDauNam TABLE (MaSP VARCHAR(50), MaTrangThai INT, SoLuong INT, GiaTri DECIMAL(19,2));
    INSERT INTO @TonDauNam
    SELECT MaSP, MaTrangThai, SUM(SoLuongDau), SUM(GiaTriDau)
    FROM DMTonKho WHERE Nam = @NamBaoCao AND (@MaKho = 0 OR MaKho = @MaKho)
    GROUP BY MaSP, MaTrangThai;

    -- 2. Biến động
    WITH BienDong AS (
        SELECT sp.MaSP, ISNULL(m.MaTrangThai, 0) AS MaTrangThai,
            SUM(CASE WHEN pn.NgayNhap >= @NgayDauNam AND pn.NgayNhap < @TuNgay THEN 1 ELSE 0 END) AS NhapDem_SL,
            SUM(CASE WHEN pn.NgayNhap >= @NgayDauNam AND pn.NgayNhap < @TuNgay THEN ctn.DonGia ELSE 0 END) AS NhapDem_GT,
            SUM(CASE WHEN pn.NgayNhap >= @TuNgay AND pn.NgayNhap <= @DenNgay THEN 1 ELSE 0 END) AS NhapTrong_SL,
            SUM(CASE WHEN pn.NgayNhap >= @TuNgay AND pn.NgayNhap <= @DenNgay THEN ctn.DonGia ELSE 0 END) AS NhapTrong_GT,
            0 AS XuatDem_SL, 0 AS XuatDem_GT, 0 AS XuatTrong_SL, 0 AS XuatTrong_GT
        FROM CTPhieuNhap ctn JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu 
        JOIN DMMay m ON ctn.MaMay = m.MaMay JOIN DMSanPham sp ON m.MaSP = sp.MaSP
        WHERE (@MaKho = 0 OR pn.MaKho = @MaKho) AND pn.NgayNhap >= @NgayDauNam 
        GROUP BY sp.MaSP, ISNULL(m.MaTrangThai, 0)
        UNION ALL
        SELECT sp.MaSP, ISNULL(m.MaTrangThai, 0) AS MaTrangThai,
            0, 0, 0, 0,
            SUM(CASE WHEN px.NgayXuat >= @NgayDauNam AND px.NgayXuat < @TuNgay THEN 1 ELSE 0 END) AS XuatDem_SL,
            SUM(CASE WHEN px.NgayXuat >= @NgayDauNam AND px.NgayXuat < @TuNgay THEN ctx.DonGia ELSE 0 END) AS XuatDem_GT,
            SUM(CASE WHEN px.NgayXuat >= @TuNgay AND px.NgayXuat <= @DenNgay THEN 1 ELSE 0 END) AS XuatTrong_SL,
            SUM(CASE WHEN px.NgayXuat >= @TuNgay AND px.NgayXuat <= @DenNgay THEN ctx.DonGia ELSE 0 END) AS XuatTrong_GT
        FROM CTPhieuXuat ctx JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu 
        JOIN DMMay m ON ctx.MaMay = m.MaMay JOIN DMSanPham sp ON m.MaSP = sp.MaSP
        WHERE (@MaKho = 0 OR px.MaKho = @MaKho) AND px.NgayXuat >= @NgayDauNam 
        GROUP BY sp.MaSP, ISNULL(m.MaTrangThai, 0)
    ),
    KetQua AS (
        SELECT 
            (ISNULL(MAX(tdn.SoLuong), 0) + SUM(ISNULL(bd.NhapDem_SL, 0)) - SUM(ISNULL(bd.XuatDem_SL, 0))) AS TonDau,
            SUM(ISNULL(bd.NhapTrong_SL, 0)) AS NhapTrong,
            SUM(ISNULL(bd.XuatTrong_SL, 0)) AS XuatTrong,
            ((ISNULL(MAX(tdn.SoLuong), 0) + SUM(ISNULL(bd.NhapDem_SL, 0)) - SUM(ISNULL(bd.XuatDem_SL, 0))) 
             + SUM(ISNULL(bd.NhapTrong_SL, 0)) - SUM(ISNULL(bd.XuatTrong_SL, 0))) AS TonCuoi,
            ((ISNULL(MAX(tdn.GiaTri), 0) + SUM(ISNULL(bd.NhapDem_GT, 0)) - SUM(ISNULL(bd.XuatDem_GT, 0))) 
             + SUM(ISNULL(bd.NhapTrong_GT, 0)) - SUM(ISNULL(bd.XuatTrong_GT, 0))) AS ThanhTien
        FROM DMSanPham sp
        LEFT JOIN (SELECT MaSP, MaTrangThai FROM BienDong UNION SELECT MaSP, MaTrangThai FROM @TonDauNam) AS Keys ON sp.MaSP = Keys.MaSP
        LEFT JOIN BienDong bd ON Keys.MaSP = bd.MaSP AND Keys.MaTrangThai = bd.MaTrangThai
        LEFT JOIN @TonDauNam tdn ON Keys.MaSP = tdn.MaSP AND Keys.MaTrangThai = tdn.MaTrangThai
        WHERE (@MaKho = 0 OR EXISTS (SELECT 1 FROM DMTonKho WHERE MaKho = @MaKho))
        GROUP BY sp.MaSP, COALESCE(bd.MaTrangThai, tdn.MaTrangThai, 0)
        HAVING (MAX(ISNULL(tdn.SoLuong, 0)) + SUM(ISNULL(bd.NhapDem_SL, 0)) - SUM(ISNULL(bd.XuatDem_SL, 0))) > 0 
            OR SUM(bd.NhapTrong_SL) > 0 OR SUM(bd.XuatTrong_SL) > 0
    )
    -- [QUAN TRỌNG]: SELECT RA NHIỀU CỘT TỔNG
    SELECT 
        COUNT(*) as TotalRows,
        SUM(TonDau) as TongTonDau,
        SUM(NhapTrong) as TongNhap,
        SUM(XuatTrong) as TongXuat,
        SUM(TonCuoi) as TongTonCuoi,
        SUM(ThanhTien) as TongTien
    FROM KetQua;
END;
GO

-- ===========================================================

CREATE OR ALTER PROCEDURE sp_LayLichSuChotSo_PhanTrang
    @Nam INT, -- Ví dụ: 2026
    @MaKho INT,
    @PageNumber INT,
    @PageSize INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @NamTruoc INT = @Nam - 1; -- Lấy năm 2025 để tìm Nhập/Xuất

    -- Tính toán biến động của năm TRƯỚC ĐÓ
    WITH BienDong AS (
        SELECT sp.MaSP, ISNULL(m.MaTrangThai, 1) AS MaTrangThai,
               SUM(CASE WHEN YEAR(pn.NgayNhap) = @NamTruoc THEN 1 ELSE 0 END) AS SL_Nhap,
               SUM(CASE WHEN YEAR(px.NgayXuat) = @NamTruoc THEN 1 ELSE 0 END) AS SL_Xuat
        FROM DMSanPham sp
        LEFT JOIN DMMay m ON sp.MaSP = m.MaSP
        LEFT JOIN CTPhieuNhap ctn ON m.MaMay = ctn.MaMay
        LEFT JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu AND (@MaKho = 0 OR pn.MaKho = @MaKho)
        LEFT JOIN CTPhieuXuat ctx ON m.MaMay = ctx.MaMay
        LEFT JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu AND (@MaKho = 0 OR px.MaKho = @MaKho)
        GROUP BY sp.MaSP, m.MaTrangThai
    )
    SELECT 
        t.MaSP,                                     -- [0]
        MAX(sp.TenSP) AS TenSP,                     -- [1]
        t.MaTrangThai,                              -- [2]
        MAX(tt.TenTrangThai) AS TenTrangThai,       -- [3]
        MAX(sp.Donvitinh) AS Donvitinh,             -- [4]
        
        -- Tồn đầu của năm 2026 thực chất là Tồn cuối của 2025
        -- Muốn hiện Nhập/Xuất của 2025 thì Tồn đầu ở đây phải là Tồn đầu của 2025
        (t.SoLuongDau - ISNULL(MAX(bd.SL_Nhap), 0) + ISNULL(MAX(bd.SL_Xuat), 0)) AS TonDauThucTe, -- [5]
        ISNULL(MAX(bd.SL_Nhap), 0) AS NhapTrong,    -- [6] -> Sẽ hiện số 10
        ISNULL(MAX(bd.SL_Xuat), 0) AS XuatTrong,    -- [7] -> Sẽ hiện số 3
        t.SoLuongDau AS TonCuoi,                   -- [8] -> Hiện số 7
        t.GiaTriDau AS ThanhTien                    -- [9]
    FROM DMTonKho t
    LEFT JOIN DMSanPham sp ON t.MaSP = sp.MaSP
    LEFT JOIN TrangThai tt ON t.MaTrangThai = tt.MaTrangThai
    LEFT JOIN BienDong bd ON t.MaSP = bd.MaSP AND t.MaTrangThai = bd.MaTrangThai
    WHERE t.Nam = @Nam AND (@MaKho = 0 OR t.MaKho = @MaKho)
    GROUP BY t.MaSP, t.MaTrangThai, t.SoLuongDau, t.GiaTriDau
    ORDER BY t.MaSP
    OFFSET (@PageNumber * @PageSize) ROWS
    FETCH NEXT @PageSize ROWS ONLY;
END;
GO

CREATE OR ALTER PROCEDURE sp_TongHopLichSuChotSo
    @Nam INT, 
    @MaKho INT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @NamTruoc INT = @Nam - 1; 

    WITH BienDong AS (
        SELECT sp.MaSP, ISNULL(m.MaTrangThai, 1) AS MaTrangThai,
               SUM(CASE WHEN YEAR(pn.NgayNhap) = @NamTruoc THEN 1 ELSE 0 END) AS SL_Nhap,
               SUM(CASE WHEN YEAR(px.NgayXuat) = @NamTruoc THEN 1 ELSE 0 END) AS SL_Xuat
        FROM DMSanPham sp
        LEFT JOIN DMMay m ON sp.MaSP = m.MaSP
        LEFT JOIN CTPhieuNhap ctn ON m.MaMay = ctn.MaMay
        LEFT JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu AND (@MaKho = 0 OR pn.MaKho = @MaKho)
        LEFT JOIN CTPhieuXuat ctx ON m.MaMay = ctx.MaMay
        LEFT JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu AND (@MaKho = 0 OR px.MaKho = @MaKho)
        GROUP BY sp.MaSP, m.MaTrangThai
    )
    
    SELECT 
        ISNULL(SUM(t.SoLuongDau - ISNULL(bd.SL_Nhap, 0) + ISNULL(bd.SL_Xuat, 0)), 0) AS TongTonDau,
        ISNULL(SUM(bd.SL_Nhap), 0) AS TongNhap,
        ISNULL(SUM(bd.SL_Xuat), 0) AS TongXuat,
        ISNULL(SUM(t.SoLuongDau), 0) AS TongTonCuoi,
        ISNULL(SUM(t.GiaTriDau), 0) AS TongTien
    FROM DMTonKho t
    LEFT JOIN BienDong bd ON t.MaSP = bd.MaSP AND t.MaTrangThai = bd.MaTrangThai
    WHERE t.Nam = @Nam AND (@MaKho = 0 OR t.MaKho = @MaKho);
END;
GO

CREATE OR ALTER PROCEDURE sp_GetDashboardStats
    @MaKho INT = 0 
AS
BEGIN
    SET NOCOUNT ON;

    -- Lấy Năm và Tháng hiện tại (Bỏ qua hoàn toàn giờ, phút, giây)
    DECLARE @NamHienTai INT = YEAR(GETDATE());
    DECLARE @ThangHienTai INT = MONTH(GETDATE());

    -- =========================================================
    -- PHẦN 1: TÍNH TOÁN DỮ LIỆU CHO 3 THẺ (CARDS) TRÊN CÙNG
    -- =========================================================
    
    -- 1. Tổng Tồn Hiện Tại 
    DECLARE @TongTonKho INT = 0;
    SELECT @TongTonKho = COUNT(MaMay) 
    FROM DMMay 
    WHERE TonKho = 1 AND (@MaKho = 0 OR MaKho = @MaKho);

    -- 2. Tổng Nhập Trong Tháng hiện tại (Lọc chuẩn theo Tháng và Năm giống Biểu đồ)
    DECLARE @NhapTrongThang INT = 0;
    SELECT @NhapTrongThang = COUNT(ctn.MaMay) 
    FROM CTPhieuNhap ctn 
    JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu 
    WHERE YEAR(pn.NgayNhap) = @NamHienTai 
      AND MONTH(pn.NgayNhap) = @ThangHienTai 
      AND (@MaKho = 0 OR pn.MaKho = @MaKho);

    -- 3. Tổng Xuất Trong Tháng hiện tại (Lọc chuẩn theo Tháng và Năm giống Biểu đồ)
    DECLARE @XuatTrongThang INT = 0;
    SELECT @XuatTrongThang = COUNT(ctx.MaMay) 
    FROM CTPhieuXuat ctx 
    JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu 
    WHERE YEAR(px.NgayXuat) = @NamHienTai 
      AND MONTH(px.NgayXuat) = @ThangHienTai 
      AND (@MaKho = 0 OR px.MaKho = @MaKho);

    -- [TRẢ VỀ KẾT QUẢ 1]: Cho thẻ Dashboard
    SELECT @TongTonKho AS TotalStock, @NhapTrongThang AS ImportMonth, @XuatTrongThang AS ExportMonth;


    -- =========================================================
    -- PHẦN 2: TÍNH TOÁN DỮ LIỆU CHO BIỂU ĐỒ (12 THÁNG)
    -- =========================================================
    
    ;WITH ThangTrongNam AS (
        SELECT 1 AS Thang UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL 
        SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL 
        SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL 
        SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
    ),
    TongHopVao AS (
        SELECT MONTH(pn.NgayNhap) as Thang, COUNT(ctn.MaMay) as SoLuong 
        FROM CTPhieuNhap ctn 
        JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu
        WHERE YEAR(pn.NgayNhap) = @NamHienTai AND (@MaKho = 0 OR pn.MaKho = @MaKho)
        GROUP BY MONTH(pn.NgayNhap)
    ),
    TongHopRa AS (
        SELECT MONTH(px.NgayXuat) as Thang, COUNT(ctx.MaMay) as SoLuong 
        FROM CTPhieuXuat ctx 
        JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu
        WHERE YEAR(px.NgayXuat) = @NamHienTai AND (@MaKho = 0 OR px.MaKho = @MaKho)
        GROUP BY MONTH(px.NgayXuat)
    )
    -- [TRẢ VỀ KẾT QUẢ 2]: Cho Biểu đồ
    SELECT 
        t.Thang AS Month,
        ISNULL(v.SoLuong, 0) AS ImportQty,
        ISNULL(r.SoLuong, 0) AS ExportQty
    FROM ThangTrongNam t
    LEFT JOIN TongHopVao v ON t.Thang = v.Thang
    LEFT JOIN TongHopRa r ON t.Thang = r.Thang
    ORDER BY t.Thang;
END;
GO



-- =======================================================
-- 4. INSERT DỮ LIỆU MẪU (SEED DATA)
-- =======================================================
PRINT N'>>> 1. BẮT ĐẦU LÀM SẠCH DỮ LIỆU CŨ...';

USE Nhat_Tien_Thanh_Kho;
GO

-- Tắt khóa ngoại tạm thời để xóa sạch dữ liệu an toàn
EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all";

-- Diệt tận gốc 2 bảng rác (Chuyển Kho)
DROP TABLE IF EXISTS dbo.ChiTietPhieuChuyen;
DROP TABLE IF EXISTS dbo.PhieuChuyen;

-- Xóa dữ liệu các bảng (Tránh lỗi FK)
DELETE FROM dbo.CTPhieuXuat;
DELETE FROM dbo.CTPhieuNhap;
DELETE FROM dbo.DMMay;
DELETE FROM dbo.PhieuXuat;
DELETE FROM dbo.PhieuNhap;
DELETE FROM dbo.DMTonKho;
DELETE FROM dbo.DMSanPham;
DELETE FROM dbo.DMDonVi;
DELETE FROM TaiKhoan;
DELETE FROM Vaitro;
DELETE FROM LoaiDonVi;
DELETE FROM TrangThai;
DELETE FROM DMHangSX;
DELETE FROM LoaiSP;
DELETE FROM Kho;

-- Bật lại khóa ngoại
EXEC sp_MSforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all";
GO

PRINT N'>>> 2. KHỞI TẠO DANH MỤC CƠ BẢN...';

-- 2.1. Khởi tạo Vai trò & Loại Đơn vị
INSERT INTO Vaitro (MaVaitro, TenVaiTro) VALUES (1, N'ROLE_ADMIN'), (2, N'ROLE_STAFF');
INSERT INTO LoaiDonVi (LoaiDonVi, TenLoai) VALUES (1, N'Nhà Cung Cấp'), (2, N'Khách Hàng');

-- 2.2. Trạng Thái (LOGIC MỚI CHUẨN)
INSERT INTO TrangThai (MaTrangThai, TenTrangThai) VALUES 
(0, N'Chưa xác định'), 
(1, N'Bình thường'),           -- Bình thường để khoảng trắng
(2, N'New'), 
(3, N'Like New'), 
(4, N'Hỏng'), 
(5, N'Xác'), 
(6, N'Thu hồi'),
(7, N'Nhập Khẩu');

-- 2.3. Tạo Kho & Tài khoản
INSERT INTO Kho (TenKho, DiaChi) VALUES (N'Kho 176 Lê Hồng Phong', N'176 Lê Hồng Phong, TP.HCM');
INSERT INTO Kho (TenKho, DiaChi) VALUES (N'Kho Quận 2', N'Khu công nghệ cao, TP Thủ Đức');

INSERT INTO TaiKhoan (TenTaiKhoan, Password, MaVaitro, MaKho) VALUES  
('admin', '$2a$12$aSiGH8LKYo/YBFwCSeoYROv/CORGKOZ4LYfyNPV8I91fNt.wtOnrC', 1, 1),
('staff', '$2a$12$aSiGH8LKYo/YBFwCSeoYROv/CORGKOZ4LYfyNPV8I91fNt.wtOnrC', 2, 1);

-- 2.4. Đơn Vị Giao Dịch
INSERT INTO DMDonVi (MaDonVi, TenDonVi, LoaiDonVi) VALUES ('NCC_MACDINH', N'Nhà Cung Cấp Mặc Định', 1);
INSERT INTO DMDonVi (MaDonVi, TenDonVi, LoaiDonVi) VALUES ('KH_MACDINH', N'Khách Hàng Mặc Định', 2);
INSERT INTO DMDonVi (MaDonVi, TenDonVi, LoaiDonVi, SoDienThoai) VALUES ('NCC_FPT', N'Công ty Cổ phần FPT Synnex', 1, '19006600');
INSERT INTO DMDonVi (MaDonVi, TenDonVi, LoaiDonVi, SoDienThoai) VALUES ('NCC_PSD', N'Công ty Dịch vụ Phân phối Dầu khí', 1, '19001234');
INSERT INTO DMDonVi (MaDonVi, TenDonVi, LoaiDonVi, SoDienThoai) VALUES ('KH_ABC', N'Công ty TNHH Thương mại ABC', 2, '0909123456');
INSERT INTO DMDonVi (MaDonVi, TenDonVi, LoaiDonVi, SoDienThoai) VALUES ('KH_XYZ', N'Cửa hàng Máy tính XYZ', 2, '0988765432');

-- 2.5. Tạo Hãng & Loại Sản Phẩm
INSERT INTO DMHangSX (TenHang) VALUES (N'Ricoh'), (N'Toshiba'), (N'Konica'), (N'Epson'), (N'Fujitsu'), (N'Sindoh'), (N'Fujifilm'), (N'Dell');
INSERT INTO LoaiSP (TenLoai) VALUES (N'Máy Văn Phòng'), (N'Laptop'), (N'Máy in');

DECLARE @MaLoai_VP INT = (SELECT TOP 1 MaLoai FROM LoaiSP WHERE TenLoai = N'Máy Văn Phòng');
DECLARE @MaKho_176 INT = 1;


PRINT N'>>> 3. BÓC TÁCH MÃ SẢN PHẨM & TRẠNG THÁI (CHO 2026)...';
IF OBJECT_ID('tempdb..#TempData') IS NOT NULL DROP TABLE #TempData;

CREATE TABLE #TempData (
    MaSP_Clean VARCHAR(50), 
    TenSP_Clean NVARCHAR(255), 
    DVT NVARCHAR(50), 
    TDK INT, NTK INT, XTK INT, TCK INT,
    MaTrangThai INT
);

-- ĐÃ ĐỔI SẴN MÃ TRẠNG THÁI SANG LOGIC MỚI VÀO ĐÂY (0->1, 1->2, 2->3, 5->6)
INSERT INTO #TempData (MaSP_Clean, TenSP_Clean, DVT, TDK, NTK, XTK, TCK, MaTrangThai) VALUES
-- =================== MÁY THU HỒI (Mã mới: 6) ===================
('C759', N'Máy Konica C759', N'Cái', 3, 0, 0, 3, 6),
('E857', N'Máy Toshiba E 857', N'Cái', 1, 0, 0, 1, 6),
('Epson3170', N'Máy Ricoh Epson 3170', N'Cái', 4, 0, 0, 4, 6),
('IMC6000', N'Máy Ricoh IMC 6000', N'Cái', 1, 0, 0, 1, 6),
('M2140', N'Máy Epson M2140', N'Cái', 3, 0, 0, 3, 6),
('MP301', N'Máy Ricoh MP 301', N'Cái', 1, 1, 0, 2, 6),
('MP305', N'Máy Ricoh MP 305', N'Cái', 1, 0, 0, 1, 6),
('MP3353', N'Máy Ricoh MP 3353', N'Cái', 1, 0, 0, 1, 6),
('MP3554', N'Máy Ricoh MP 3554', N'Cái', 2, 3, 1, 4, 6),
('MP5001', N'Máy Ricoh MP 5001', N'Cái', 1, 0, 0, 1, 6),
('MP5002', N'Máy Ricoh MP 5002', N'Cái', 3, 3, 6, 0, 6),
('MP5054', N'Máy Ricoh MP 5054', N'Cái', 1, 0, 1, 0, 6),
('MP6054', N'Máy Ricoh MP 6054', N'Cái', 2, 1, 1, 2, 6),
('MP6055', N'Máy Ricoh MP 6055', N'Cái', 2, 0, 1, 1, 6),
('MP7503', N'Máy Ricoh MP 7503', N'Cái', 1, 0, 0, 1, 6),
('R-MP3555', N'Máy Ricoh MP 3555', N'Cái', 0, 1, 0, 1, 6),

-- =================== MÁY MỚI (Mã mới: 2) ===================
('F8150', N'Máy scan Fujitsu fi-8150', N'Cái', 3, 0, 0, 3, 2), 
('F8170', N'Máy scan Fujitsu fi-8170', N'Cái', 2, 0, 0, 2, 2),
('R-FJ2150', N'Máy Fujifilm 2150', N'Cái', 3, 0, 2, 1, 2),
('R-GS3026', N'Ricoh GS3026', N'Cái', 1, 1, 1, 1, 2),
('R-IM2500', N'Máy Ricoh IM 2500', N'Cái', 1, 0, 0, 1, 2),
('S-N410', N'Máy Sindoh N410', N'Cái', 3, 0, 0, 3, 2),
('S-N411', N'Máy Sindoh N411', N'Cái', 4, 0, 0, 4, 2),
('S-N611', N'Máy Sindoh N611', N'Cái', 4, 0, 0, 4, 2),
('S-N612', N'Máy Sindoh N612', N'Cái', 11, 0, 0, 11, 2),
('S-N613', N'Máy Sindoh N613', N'Cái', 9, 0, 0, 9, 2),
('SD-620DN', N'Máy in Sindoh 620DN', N'Cái', 7, 0, 0, 7, 2),
('T-E2329A', N'Máy Toshiba E 2329A', N'Cái', 5, 0, 0, 5, 2),
('T-E2528A', N'Máy Toshiba E 2528A', N'Cái', 6, 0, 1, 5, 2),
('T-E2829A', N'Máy Toshiba E 2829A', N'Cái', 5, 0, 0, 5, 2),
('T-E3028A', N'Máy Toshiba E 3028A', N'Cái', 16, 0, 13, 3, 2),
('T-E3528A', N'Máy Toshiba E 3528A', N'Cái', 9, 0, 8, 1, 2),
('T-E4528A', N'Máy Toshiba E 4528A', N'Cái', 5, 0, 1, 4, 2),
('T-E5528A', N'Máy Toshiba E 5528A', N'Cái', 5, 0, 2, 3, 2),
('T-E6528A', N'Máy Toshiba E 6528A', N'Cái', 3, 0, 1, 2, 2),

-- =================== MÁY LIKE NEW (Mã mới: 3) ===================
('K-C759', N'Máy Konica C759', N'Cái', 10, 2, 2, 10, 3),
('R-5055', N'Máy Ricoh MP 5055', N'Cái', 2, 3, 4, 1, 3),
('R-6055', N'Máy Ricoh MP 6055', N'Cái', 9, 0, 0, 9, 3),
('R-C4500', N'Máy Ricoh IM C4500', N'Cái', 0, 3, 1, 2, 3),
('R-MP3555', N'Máy Ricoh MP 3555', N'Cái', 5, 5, 3, 7, 3),
('R-MP6055', N'Máy Ricoh MP 6055', N'Cái', 5, 4, 5, 4, 3),
('R-MP7503', N'Máy Ricoh MP 7503', N'Cái', 6, 0, 5, 1, 3),

-- =================== MÁY BÌNH THƯỜNG (Mã mới: 1) ===================
('R-C2004', N'Máy Ricoh MP C2004', N'Cái', 0, 4, 3, 1, 1),
('R-C3004', N'Máy Ricoh MP C3004', N'Cái', 0, 3, 1, 2, 1),
('R-Fi6800', N'Máy Ricoh Fi 6800', N'Cái', 2, 0, 0, 2, 1),
('R-IM350F', N'Máy Ricoh IM 350F', N'Cái', 2, 0, 1, 1, 1),
('R-IM4000', N'Máy Ricoh IM 4000', N'Cái', 2, 0, 0, 2, 1),
('R-IM430', N'Máy Ricoh IM 430', N'Cái', 2, 0, 0, 2, 1),
('R-IM430F', N'Máy Ricoh IM 430F', N'Cái', 0, 3, 0, 3, 1),
('R-IM5000', N'Máy Ricoh IM 5000', N'Cái', 2, 0, 0, 2, 1),
('R-IM7000', N'Máy Ricoh IM 7000', N'Cái', 1, 0, 0, 1, 1),
('R-IM8000', N'Máy Ricoh IM 8000', N'Cái', 1, 0, 1, 0, 1),
('R-IMC2000', N'Máy Ricoh IM C2000', N'Cái', 0, 1, 0, 1, 1),
('R-IMC3000', N'Máy Ricoh IM C3000', N'Cái', 5, 0, 2, 3, 1),
('R-IMC300F', N'Máy Ricoh IM C300F', N'Cái', 1, 0, 0, 1, 1),
('R-IMC4500', N'Máy Ricoh IM C4500', N'Cái', 1, 0, 0, 1, 1),
('R-IMC6000', N'Máy Ricoh IM C6000', N'Cái', 1, 0, 1, 0, 1),
('R-MP2501SP', N'Máy Ricoh MP 2501SP', N'Cái', 0, 2, 0, 2, 1),
('R-MP2554', N'Máy Ricoh MP 2554', N'Cái', 1, 0, 1, 0, 1),
('R-MP2555', N'Máy Ricoh MP 2555', N'Cái', 2, 1, 2, 1, 1),
('R-MP3554', N'Máy Ricoh MP 3554', N'Cái', 1, 0, 0, 1, 1),
('R-MP402', N'Máy Ricoh MP 402', N'Cái', 0, 3, 1, 2, 1),
('R-MP4054', N'Máy Ricoh MP 4054', N'Cái', 2, 0, 0, 2, 1),
('R-MP4055', N'Máy Ricoh MP 4055', N'Cái', 3, 2, 0, 5, 1),
('R-MP5002', N'Máy Ricoh MP 5002', N'Cái', 1, 0, 1, 0, 1),
('R-MP5054', N'Máy Ricoh MP 5054', N'Cái', 3, 0, 1, 2, 1),
('R-MP5055', N'Máy Ricoh MP 5055', N'Cái', 1, 0, 0, 1, 1),
('R-MP6055', N'Máy Ricoh MP 6055', N'Cái', 0, 0, 0, 0, 1), 
('R-MP9002', N'Máy Ricoh MP 9002', N'Cái', 1, 0, 0, 1, 1),
('R-MP9003', N'Máy Ricoh MP 9003', N'Cái', 2, 0, 0, 2, 1),
('R-MPC307', N'Máy Ricoh MPC 307', N'Cái', 0, 3, 0, 3, 1),
('R-SPC440', N'Máy Ricoh SP C440', N'Cái', 1, 0, 1, 0, 1),
('R-W3601', N'Máy Ricoh MP W3601', N'Cái', 1, 0, 0, 1, 1),
('R-W6700', N'Máy Ricoh W6700', N'Cái', 1, 0, 0, 1, 1),
('R-W7100', N'Máy Ricoh W7100', N'Cái', 1, 0, 0, 1, 1),
('SD-IMC2000', N'Máy Ricoh IM C2000', N'Cái', 1, 0, 0, 1, 1),
('SD-IMC2500', N'Máy Ricoh IM C2500', N'Cái', 1, 0, 0, 1, 1),
('SD-L15160', N'Máy Epson L15160', N'Cái', 1, 0, 0, 1, 1),
('T-A2528', N'Máy Toshiba E 2528A', N'Cái', 1, 0, 0, 1, 1),
('T-A3508', N'Máy Toshiba ES 3508A', N'Cái', 2, 0, 0, 2, 1),
('T-A5518', N'Máy Toshiba E 5518A', N'Cái', 1, 0, 1, 0, 1),
('T-AC3515', N'Máy Toshiba ES 3515C', N'Cái', 1, 0, 0, 1, 1),
('T-E2018A', N'Máy Toshiba E 2018A', N'Cái', 0, 1, 0, 1, 1),
('T-E3518A', N'Máy Toshiba E 3518A', N'Cái', 1, 1, 1, 1, 1),
('T-E5528A', N'Máy Toshiba E 5528A', N'Cái', 1, 0, 0, 1, 1);

-- Danh mục sản phẩm 
INSERT INTO DMSanPham (MaSP, TenSP, Donvitinh, MaLoai, MaHang, SoLuong)
SELECT DISTINCT MaSP_Clean, TenSP_Clean, DVT, @MaLoai_VP, 
       CASE 
           WHEN TenSP_Clean LIKE '%Ricoh%' THEN (SELECT MaHang FROM DMHangSX WHERE TenHang = 'Ricoh')
           WHEN TenSP_Clean LIKE '%Toshiba%' THEN (SELECT MaHang FROM DMHangSX WHERE TenHang = 'Toshiba')
           WHEN TenSP_Clean LIKE '%Konica%' THEN (SELECT MaHang FROM DMHangSX WHERE TenHang = 'Konica')
           WHEN TenSP_Clean LIKE '%Epson%' THEN (SELECT MaHang FROM DMHangSX WHERE TenHang = 'Epson')
           WHEN TenSP_Clean LIKE '%Fujitsu%' THEN (SELECT MaHang FROM DMHangSX WHERE TenHang = 'Fujitsu')
           WHEN TenSP_Clean LIKE '%Sindoh%' THEN (SELECT MaHang FROM DMHangSX WHERE TenHang = 'Sindoh')
           WHEN TenSP_Clean LIKE '%Fujifilm%' THEN (SELECT MaHang FROM DMHangSX WHERE TenHang = 'Fujifilm')
           ELSE (SELECT TOP 1 MaHang FROM DMHangSX) 
       END, 0
FROM #TempData;

PRINT N'>>> 4. TIẾN HÀNH TẠO PHIẾU VÀ MÁY (DỮ LIỆU 2026)...';

DECLARE @PN_TDK VARCHAR(50) = 'PN_TDK_2026';
DECLARE @PN_NTK VARCHAR(50) = 'PN_NTK_2026';
DECLARE @PX_XTK VARCHAR(50) = 'PX_XTK_2026';

INSERT INTO PhieuNhap (SoPhieu, MaKho, MaDonVi, NgayNhap, GhiChu, TongTien) VALUES (@PN_TDK, @MaKho_176, 'NCC_MACDINH', '2026-01-31 10:00:00', N'Nhập mô phỏng Tồn Đầu Kỳ', 0);
INSERT INTO PhieuNhap (SoPhieu, MaKho, MaDonVi, NgayNhap, GhiChu, TongTien) VALUES (@PN_NTK, @MaKho_176, 'NCC_MACDINH', '2026-02-15 10:00:00', N'Nhập trong kỳ tháng 02/2026', 0);
INSERT INTO PhieuXuat (SoPhieu, MaKho, MaDonVi, NgayXuat, GhiChu, TongTien) VALUES (@PX_XTK, @MaKho_176, 'KH_MACDINH', '2026-02-25 10:00:00', N'Xuất trong kỳ tháng 02/2026', 0);

DECLARE @MaSP VARCHAR(50), @TDK INT, @NTK INT, @XTK INT, @TrangThai INT;
DECLARE @LastMaSP VARCHAR(50) = '';
DECLARE @Idx_TDK INT = 1;
DECLARE @Idx_NTK INT = 1;

DECLARE cur CURSOR FOR
SELECT MaSP_Clean, ISNULL(TDK, 0), ISNULL(NTK, 0), ISNULL(XTK, 0), MaTrangThai
FROM #TempData
ORDER BY MaSP_Clean, MaTrangThai; 

OPEN cur;
FETCH NEXT FROM cur INTO @MaSP, @TDK, @NTK, @XTK, @TrangThai;

WHILE @@FETCH_STATUS = 0
BEGIN
    IF @MaSP <> @LastMaSP
    BEGIN
        SET @Idx_TDK = 1;
        SET @Idx_NTK = 1;
        SET @LastMaSP = @MaSP;
    END
    DECLARE @k INT;

    -- Tồn Đầu Kỳ
    IF @TDK > 0
    BEGIN
        SET @k = 1;
        WHILE @k <= @TDK
        BEGIN
            DECLARE @MaMayTDK VARCHAR(50) = @MaSP + '202601' + RIGHT('000' + CAST(@Idx_TDK AS VARCHAR), 3);
            INSERT INTO DMMay (MaMay, MaSP, MaKho, SoSeri, NgayTao, MaTrangThai, SoPhieuNhap, TonKho)
            VALUES (@MaMayTDK, @MaSP, @MaKho_176, @MaMayTDK, '2026-01-31 10:00:00', @TrangThai, @PN_TDK, 1);
            INSERT INTO CTPhieuNhap (SoPhieu, MaSP, MaMay, DonGia, NgayTao)
            VALUES (@PN_TDK, @MaSP, @MaMayTDK, 0, '2026-01-31 10:00:00');
            SET @k = @k + 1; SET @Idx_TDK = @Idx_TDK + 1;
        END
    END

    -- Nhập Trong Kỳ
    IF @NTK > 0
    BEGIN
        SET @k = 1;
        WHILE @k <= @NTK
        BEGIN
            DECLARE @MaMayNTK VARCHAR(50) = @MaSP + '202602' + RIGHT('000' + CAST(@Idx_NTK AS VARCHAR), 3);
            INSERT INTO DMMay (MaMay, MaSP, MaKho, SoSeri, NgayTao, MaTrangThai, SoPhieuNhap, TonKho)
            VALUES (@MaMayNTK, @MaSP, @MaKho_176, @MaMayNTK, '2026-02-15 10:00:00', @TrangThai, @PN_NTK, 1);
            INSERT INTO CTPhieuNhap (SoPhieu, MaSP, MaMay, DonGia, NgayTao)
            VALUES (@PN_NTK, @MaSP, @MaMayNTK, 0, '2026-02-15 10:00:00');
            SET @k = @k + 1; SET @Idx_NTK = @Idx_NTK + 1;
        END
    END

    -- Xuất Trong Kỳ
    IF @XTK > 0
    BEGIN
        DECLARE @MayXuat TABLE (MaMay VARCHAR(50));
        DELETE FROM @MayXuat;
        INSERT INTO @MayXuat
        SELECT TOP (@XTK) MaMay FROM DMMay 
        WHERE MaSP = @MaSP AND MaTrangThai = @TrangThai AND TonKho = 1 AND MaKho = @MaKho_176 
        ORDER BY NgayTao ASC;

        INSERT INTO CTPhieuXuat (SoPhieu, MaSP, MaMay, DonGia, NgayTao)
        SELECT @PX_XTK, @MaSP, MaMay, 0, '2026-02-25 10:00:00' FROM @MayXuat; 
        UPDATE DMMay SET TonKho = 0 WHERE MaMay IN (SELECT MaMay FROM @MayXuat);
    END
    FETCH NEXT FROM cur INTO @MaSP, @TDK, @NTK, @XTK, @TrangThai;
END
CLOSE cur; DEALLOCATE cur;

-- Chốt sổ tổng kết số lượng vào DMSanPham
UPDATE PhieuNhap SET SoLuong = (SELECT COUNT(*) FROM CTPhieuNhap WHERE SoPhieu = PhieuNhap.SoPhieu);
UPDATE PhieuXuat SET SoLuong = (SELECT COUNT(*) FROM CTPhieuXuat WHERE SoPhieu = PhieuXuat.SoPhieu);
UPDATE DMSanPham SET SoLuong = (SELECT COUNT(*) FROM DMMay WHERE DMMay.MaSP = DMSanPham.MaSP AND TonKho = 1);

PRINT N'>>> HOÀN TẤT TẤT CẢ DỮ LIỆU! CƠ SỞ DỮ LIỆU ĐÃ SẴN SÀNG.';
GO