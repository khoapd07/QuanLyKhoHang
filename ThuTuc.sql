USE Nhat_Tien_Thanh_Kho;
GO

-- =======================================================
-- 1. BÁO CÁO XUẤT NHẬP TỒN (THỐNG KÊ LÕI)
-- =======================================================
IF OBJECT_ID('sp_BaoCaoXuatNhapTon_PhanTrang', 'P') IS NOT NULL
    DROP PROCEDURE sp_BaoCaoXuatNhapTon_PhanTrang;
GO

CREATE PROCEDURE sp_BaoCaoXuatNhapTon_PhanTrang
    @MaKho INT, @TuNgay DATE, @DenNgay DATETIME, @LoaiLoc INT, @PageNumber INT, @PageSize INT    
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @NamBaoCao INT = YEAR(@TuNgay);
    DECLARE @NgayDauNam DATE = DATEFROMPARTS(@NamBaoCao, 1, 1);

    -- BƯỚC 1: Lấy Tồn Đầu Năm (từ bảng rút gọn)
    DECLARE @TonDauNam TABLE (MaKho INT, MaSP VARCHAR(50), MaTrangThai INT, SoLuong INT, GiaTri DECIMAL(19,2));
    INSERT INTO @TonDauNam
    SELECT MaKho, MaSP, MaTrangThai, SUM(SoLuong), SUM(GiaTri) 
    FROM DMTonKho 
    WHERE Nam = @NamBaoCao AND (@MaKho = 0 OR MaKho = @MaKho)
    GROUP BY MaKho, MaSP, MaTrangThai;

    -- BƯỚC 2: Tính Biến Động (Trước Kỳ và Trong Kỳ)
    ;WITH BienDong AS (
        SELECT MaKho, MaSP, MaTrangThai, 
            SUM(SL_Nhap_TruocKy) AS SL_Nhap_TruocKy, SUM(GT_Nhap_TruocKy) AS GT_Nhap_TruocKy,
            SUM(SL_Xuat_TruocKy) AS SL_Xuat_TruocKy, SUM(GT_Xuat_TruocKy) AS GT_Xuat_TruocKy,
            SUM(SL_Nhap_TrongKy) AS SL_Nhap_TrongKy, SUM(GT_Nhap_TrongKy) AS GT_Nhap_TrongKy,
            SUM(SL_Xuat_TrongKy) AS SL_Xuat_TrongKy, SUM(GT_Xuat_TrongKy) AS GT_Xuat_TrongKy
        FROM (
            -- DỮ LIỆU NHẬP
            SELECT pn.MaKho, sp.MaSP, ISNULL(m.MaTrangThai, 1) AS MaTrangThai,
                SUM(CASE WHEN pn.NgayNhap >= @NgayDauNam AND CAST(pn.NgayNhap AS DATE) < @TuNgay THEN 1 ELSE 0 END) AS SL_Nhap_TruocKy,
                SUM(CASE WHEN pn.NgayNhap >= @NgayDauNam AND CAST(pn.NgayNhap AS DATE) < @TuNgay THEN ctn.DonGia ELSE 0 END) AS GT_Nhap_TruocKy,
                0 AS SL_Xuat_TruocKy, 0 AS GT_Xuat_TruocKy,
                SUM(CASE WHEN CAST(pn.NgayNhap AS DATE) >= @TuNgay AND CAST(pn.NgayNhap AS DATE) <= CAST(@DenNgay AS DATE) THEN 1 ELSE 0 END) AS SL_Nhap_TrongKy,
                SUM(CASE WHEN CAST(pn.NgayNhap AS DATE) >= @TuNgay AND CAST(pn.NgayNhap AS DATE) <= CAST(@DenNgay AS DATE) THEN ctn.DonGia ELSE 0 END) AS GT_Nhap_TrongKy,
                0 AS SL_Xuat_TrongKy, 0 AS GT_Xuat_TrongKy
            FROM CTPhieuNhap ctn JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu JOIN DMMay m ON ctn.MaMay = m.MaMay JOIN DMSanPham sp ON m.MaSP = sp.MaSP
            WHERE (@MaKho = 0 OR pn.MaKho = @MaKho) AND pn.NgayNhap >= @NgayDauNam GROUP BY pn.MaKho, sp.MaSP, m.MaTrangThai
            UNION ALL
            -- DỮ LIỆU XUẤT
            SELECT px.MaKho, sp.MaSP, ISNULL(m.MaTrangThai, 1) AS MaTrangThai,
                0, 0,
                SUM(CASE WHEN px.NgayXuat >= @NgayDauNam AND CAST(px.NgayXuat AS DATE) < @TuNgay THEN 1 ELSE 0 END) AS SL_Xuat_TruocKy,
                SUM(CASE WHEN px.NgayXuat >= @NgayDauNam AND CAST(px.NgayXuat AS DATE) < @TuNgay THEN ctx.DonGia ELSE 0 END) AS GT_Xuat_TruocKy,
                0, 0,
                SUM(CASE WHEN CAST(px.NgayXuat AS DATE) >= @TuNgay AND CAST(px.NgayXuat AS DATE) <= CAST(@DenNgay AS DATE) THEN 1 ELSE 0 END) AS SL_Xuat_TrongKy,
                SUM(CASE WHEN CAST(px.NgayXuat AS DATE) >= @TuNgay AND CAST(px.NgayXuat AS DATE) <= CAST(@DenNgay AS DATE) THEN ctx.DonGia ELSE 0 END) AS GT_Xuat_TrongKy
            FROM CTPhieuXuat ctx JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu JOIN DMMay m ON ctx.MaMay = m.MaMay JOIN DMSanPham sp ON m.MaSP = sp.MaSP
            WHERE (@MaKho = 0 OR px.MaKho = @MaKho) AND px.NgayXuat >= @NgayDauNam GROUP BY px.MaKho, sp.MaSP, m.MaTrangThai
        ) AS tmp GROUP BY MaKho, MaSP, MaTrangThai
    ),
    -- BƯỚC 3: Công thức Tồn Cuối
    TongKet AS (
        SELECT 
            COALESCE(bd.MaKho, tdn.MaKho) AS MaKho, COALESCE(bd.MaSP, tdn.MaSP) AS MaSP, COALESCE(bd.MaTrangThai, tdn.MaTrangThai, 1) AS MaTrangThai,
            
            -- TỒN ĐẦU KỲ = Tồn năm + Nhập trước kỳ - Xuất trước kỳ
            (ISNULL(MAX(tdn.SoLuong), 0) + SUM(ISNULL(bd.SL_Nhap_TruocKy, 0)) - SUM(ISNULL(bd.SL_Xuat_TruocKy, 0))) AS TonDau_SL,
            (ISNULL(MAX(tdn.GiaTri), 0) + SUM(ISNULL(bd.GT_Nhap_TruocKy, 0)) - SUM(ISNULL(bd.GT_Xuat_TruocKy, 0))) AS TonDau_GT,
            
            -- TRONG KỲ
            SUM(ISNULL(bd.SL_Nhap_TrongKy, 0)) AS NhapTrong_SL, SUM(ISNULL(bd.GT_Nhap_TrongKy, 0)) AS NhapTrong_GT,
            SUM(ISNULL(bd.SL_Xuat_TrongKy, 0)) AS XuatTrong_SL, SUM(ISNULL(bd.GT_Xuat_TrongKy, 0)) AS XuatTrong_GT
        FROM BienDong bd FULL OUTER JOIN @TonDauNam tdn ON bd.MaKho = tdn.MaKho AND bd.MaSP = tdn.MaSP AND bd.MaTrangThai = tdn.MaTrangThai
        GROUP BY COALESCE(bd.MaKho, tdn.MaKho), COALESCE(bd.MaSP, tdn.MaSP), COALESCE(bd.MaTrangThai, tdn.MaTrangThai, 1)
    )
    SELECT 
        sp.MaSP, sp.TenSP, tk.MaTrangThai, ISNULL(tt.TenTrangThai, N'') AS TenTrangThai, sp.Donvitinh,
        tk.TonDau_SL AS TonDau, tk.NhapTrong_SL AS NhapTrong, tk.XuatTrong_SL AS XuatTrong,
        -- TỒN CUỐI KỲ = Tồn đầu kỳ + Nhập - Xuất
        (tk.TonDau_SL + tk.NhapTrong_SL - tk.XuatTrong_SL) AS TonCuoi, 
        (tk.TonDau_GT + tk.NhapTrong_GT - tk.XuatTrong_GT) AS ThanhTien
    FROM TongKet tk JOIN DMSanPham sp ON tk.MaSP = sp.MaSP LEFT JOIN TrangThai tt ON tk.MaTrangThai = tt.MaTrangThai
    WHERE (@MaKho = 0 OR tk.MaKho = @MaKho) AND (tk.TonDau_SL > 0 OR tk.NhapTrong_SL > 0 OR tk.XuatTrong_SL > 0 OR (tk.TonDau_SL + tk.NhapTrong_SL - tk.XuatTrong_SL) > 0)
    ORDER BY sp.MaSP, tk.MaTrangThai OFFSET (@PageNumber * @PageSize) ROWS FETCH NEXT @PageSize ROWS ONLY;
END;
GO

-- =======================================================
-- 2. ĐẾM DÒNG CHO BÁO CÁO
-- =======================================================
IF OBJECT_ID('sp_Count_BaoCaoXuatNhapTon', 'P') IS NOT NULL
    DROP PROCEDURE sp_Count_BaoCaoXuatNhapTon;
GO

CREATE PROCEDURE sp_Count_BaoCaoXuatNhapTon
    @MaKho INT, @TuNgay DATE, @DenNgay DATETIME, @LoaiLoc INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @NamBaoCao INT = YEAR(@TuNgay), @NgayDauNam DATE = DATEFROMPARTS(YEAR(@TuNgay), 1, 1);

    DECLARE @TonDauNam TABLE (MaKho INT, MaSP VARCHAR(50), MaTrangThai INT, SoLuong INT, GiaTri DECIMAL(19,2));
    INSERT INTO @TonDauNam SELECT MaKho, MaSP, MaTrangThai, SUM(SoLuong), SUM(GiaTri) FROM DMTonKho WHERE Nam = @NamBaoCao AND (@MaKho = 0 OR MaKho = @MaKho) GROUP BY MaKho, MaSP, MaTrangThai;

    ;WITH BienDong AS (
        SELECT MaKho, MaSP, MaTrangThai, SUM(NhapDem_SL) AS NhapDem_SL, SUM(XuatDem_SL) AS XuatDem_SL, SUM(NhapTrong_SL) AS NhapTrong_SL, SUM(XuatTrong_SL) AS XuatTrong_SL
        FROM (
            SELECT pn.MaKho, sp.MaSP, ISNULL(m.MaTrangThai, 1) AS MaTrangThai,
                SUM(CASE WHEN pn.NgayNhap >= @NgayDauNam AND CAST(pn.NgayNhap AS DATE) < @TuNgay THEN 1 ELSE 0 END) AS NhapDem_SL, 0 AS XuatDem_SL,
                SUM(CASE WHEN CAST(pn.NgayNhap AS DATE) >= @TuNgay AND CAST(pn.NgayNhap AS DATE) <= CAST(@DenNgay AS DATE) THEN 1 ELSE 0 END) AS NhapTrong_SL, 0 AS XuatTrong_SL
            FROM CTPhieuNhap ctn JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu JOIN DMMay m ON ctn.MaMay = m.MaMay JOIN DMSanPham sp ON m.MaSP = sp.MaSP WHERE (@MaKho = 0 OR pn.MaKho = @MaKho) AND pn.NgayNhap >= @NgayDauNam GROUP BY pn.MaKho, sp.MaSP, m.MaTrangThai
            UNION ALL
            SELECT px.MaKho, sp.MaSP, ISNULL(m.MaTrangThai, 1) AS MaTrangThai, 0,
                SUM(CASE WHEN px.NgayXuat >= @NgayDauNam AND CAST(px.NgayXuat AS DATE) < @TuNgay THEN 1 ELSE 0 END) AS XuatDem_SL, 0,
                SUM(CASE WHEN CAST(px.NgayXuat AS DATE) >= @TuNgay AND CAST(px.NgayXuat AS DATE) <= CAST(@DenNgay AS DATE) THEN 1 ELSE 0 END) AS XuatTrong_SL
            FROM CTPhieuXuat ctx JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu JOIN DMMay m ON ctx.MaMay = m.MaMay JOIN DMSanPham sp ON m.MaSP = sp.MaSP WHERE (@MaKho = 0 OR px.MaKho = @MaKho) AND px.NgayXuat >= @NgayDauNam GROUP BY px.MaKho, sp.MaSP, m.MaTrangThai
        ) AS tmp GROUP BY MaKho, MaSP, MaTrangThai
    ),
    TongKet AS (
        SELECT COALESCE(bd.MaKho, tdn.MaKho) AS MaKho,
            (ISNULL(MAX(tdn.SoLuong), 0) + SUM(ISNULL(bd.NhapDem_SL, 0)) - SUM(ISNULL(bd.XuatDem_SL, 0))) AS TonDau_SL,
            SUM(ISNULL(bd.NhapTrong_SL, 0)) AS NhapTrong_SL, SUM(ISNULL(bd.XuatTrong_SL, 0)) AS XuatTrong_SL
        FROM BienDong bd FULL OUTER JOIN @TonDauNam tdn ON bd.MaKho = tdn.MaKho AND bd.MaSP = tdn.MaSP AND bd.MaTrangThai = tdn.MaTrangThai
        GROUP BY COALESCE(bd.MaKho, tdn.MaKho), COALESCE(bd.MaSP, tdn.MaSP), COALESCE(bd.MaTrangThai, tdn.MaTrangThai, 1)
        HAVING (MAX(ISNULL(tdn.SoLuong, 0)) + SUM(ISNULL(bd.NhapDem_SL, 0)) - SUM(ISNULL(bd.XuatDem_SL, 0))) > 0 OR SUM(ISNULL(bd.NhapTrong_SL, 0)) > 0 OR SUM(ISNULL(bd.XuatTrong_SL, 0)) > 0
    )
    SELECT COUNT(*) as TotalRows FROM TongKet;
END;
GO

-- =======================================================
-- 3. CHỐT SỔ ("MƯỢN THẾ" CỦA THỐNG KÊ)
-- =======================================================
IF OBJECT_ID('sp_ChotSoTonDauNam', 'P') IS NOT NULL
    DROP PROCEDURE sp_ChotSoTonDauNam;
GO

CREATE PROCEDURE sp_ChotSoTonDauNam
    @NamCanChot INT, @MaKho INT       
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @NamTinhToan INT = @NamCanChot - 1; 
    DECLARE @TuNgay DATE = DATEFROMPARTS(@NamTinhToan, 1, 1);
    DECLARE @DenNgay DATETIME = DATETIMEFROMPARTS(@NamTinhToan, 12, 31, 23, 59, 59, 999);

    IF @MaKho = 0 DELETE FROM DMTonKho WHERE Nam = @NamCanChot;
    ELSE DELETE FROM DMTonKho WHERE Nam = @NamCanChot AND MaKho = @MaKho;

    DECLARE @TonDauNamCu TABLE (MaKho INT, MaSP VARCHAR(50), MaTrangThai INT, SoLuong INT, GiaTri DECIMAL(19,2));
    INSERT INTO @TonDauNamCu
    SELECT MaKho, MaSP, MaTrangThai, SUM(SoLuong), SUM(GiaTri)
    FROM DMTonKho WHERE Nam = @NamTinhToan AND (@MaKho = 0 OR MaKho = @MaKho)
    GROUP BY MaKho, MaSP, MaTrangThai;

    ;WITH BienDongNamCu AS (
        SELECT MaSP, MaTrangThai, MaKho, SUM(SL_Nhap) AS SL_Nhap, SUM(GT_Nhap) AS GT_Nhap, SUM(SL_Xuat) AS SL_Xuat, SUM(GT_Xuat) AS GT_Xuat FROM (
            SELECT sp.MaSP, ISNULL(m.MaTrangThai, 1) AS MaTrangThai, pn.MaKho, COUNT(*) AS SL_Nhap, SUM(ctn.DonGia) AS GT_Nhap, 0 AS SL_Xuat, 0 AS GT_Xuat
            FROM CTPhieuNhap ctn JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu JOIN DMMay m ON ctn.MaMay = m.MaMay JOIN DMSanPham sp ON m.MaSP = sp.MaSP
            WHERE CAST(pn.NgayNhap AS DATE) >= @TuNgay AND CAST(pn.NgayNhap AS DATE) <= CAST(@DenNgay AS DATE) AND (@MaKho = 0 OR pn.MaKho = @MaKho) GROUP BY sp.MaSP, m.MaTrangThai, pn.MaKho
            UNION ALL
            SELECT sp.MaSP, ISNULL(m.MaTrangThai, 1) AS MaTrangThai, px.MaKho, 0, 0, COUNT(*) AS SL_Xuat, SUM(ctx.DonGia) AS GT_Xuat
            FROM CTPhieuXuat ctx JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu JOIN DMMay m ON ctx.MaMay = m.MaMay JOIN DMSanPham sp ON m.MaSP = sp.MaSP
            WHERE CAST(px.NgayXuat AS DATE) >= @TuNgay AND CAST(px.NgayXuat AS DATE) <= CAST(@DenNgay AS DATE) AND (@MaKho = 0 OR px.MaKho = @MaKho) GROUP BY sp.MaSP, m.MaTrangThai, px.MaKho
        ) AS tmp GROUP BY MaSP, MaTrangThai, MaKho
    ),
    TongKetNamCu AS (
        SELECT 
            COALESCE(td.MaKho, bd.MaKho) AS MaKho, COALESCE(td.MaSP, bd.MaSP) AS MaSP, COALESCE(td.MaTrangThai, bd.MaTrangThai) AS MaTrangThai,
            (ISNULL(MAX(td.SoLuong), 0) + SUM(ISNULL(bd.SL_Nhap, 0)) - SUM(ISNULL(bd.SL_Xuat, 0))) AS TonCuoiNamCu_SL,
            (ISNULL(MAX(td.GiaTri), 0) + SUM(ISNULL(bd.GT_Nhap, 0)) - SUM(ISNULL(bd.GT_Xuat, 0))) AS TonCuoiNamCu_GT
        FROM @TonDauNamCu td FULL OUTER JOIN BienDongNamCu bd ON td.MaKho = bd.MaKho AND td.MaSP = bd.MaSP AND td.MaTrangThai = bd.MaTrangThai
        GROUP BY COALESCE(td.MaKho, bd.MaKho), COALESCE(td.MaSP, bd.MaSP), COALESCE(td.MaTrangThai, bd.MaTrangThai)
    )
    INSERT INTO DMTonKho (Nam, MaKho, MaSP, MaTrangThai, SoLuong, GiaTri)
    SELECT @NamCanChot, MaKho, MaSP, MaTrangThai, TonCuoiNamCu_SL, TonCuoiNamCu_GT
    FROM TongKetNamCu WHERE TonCuoiNamCu_SL > 0; 
END;
GO

-- =======================================================
-- 4. XEM LỊCH SỬ CHỐT SỔ
-- =======================================================
IF OBJECT_ID('sp_LayLichSuChotSo_PhanTrang', 'P') IS NOT NULL
    DROP PROCEDURE sp_LayLichSuChotSo_PhanTrang;
GO

CREATE PROCEDURE sp_LayLichSuChotSo_PhanTrang
    @Nam INT, @MaKho INT, @PageNumber INT, @PageSize INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        t.MaSP, MAX(sp.TenSP) AS TenSP, t.MaTrangThai, MAX(tt.TenTrangThai) AS TenTrangThai, MAX(sp.Donvitinh) AS Donvitinh,
        t.SoLuong AS TonDau, 0 AS NhapTrong, 0 AS XuatTrong, t.SoLuong AS TonCuoi, t.GiaTri AS ThanhTien
    FROM DMTonKho t LEFT JOIN DMSanPham sp ON t.MaSP = sp.MaSP LEFT JOIN TrangThai tt ON t.MaTrangThai = tt.MaTrangThai
    WHERE t.Nam = @Nam AND (@MaKho = 0 OR t.MaKho = @MaKho)
    GROUP BY t.MaSP, t.MaTrangThai, t.SoLuong, t.GiaTri
    ORDER BY t.MaSP OFFSET (@PageNumber * @PageSize) ROWS FETCH NEXT @PageSize ROWS ONLY;
END;
GO

-- =======================================================
-- 5. TỔNG HỢP LỊCH SỬ CHỐT SỔ
-- =======================================================
IF OBJECT_ID('sp_TongHopLichSuChotSo', 'P') IS NOT NULL
    DROP PROCEDURE sp_TongHopLichSuChotSo;
GO

CREATE PROCEDURE sp_TongHopLichSuChotSo
    @Nam INT, @MaKho INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT ISNULL(SUM(t.SoLuong), 0) AS TongTonDau, 0 AS TongNhap, 0 AS TongXuat, ISNULL(SUM(t.SoLuong), 0) AS TongTonCuoi, ISNULL(SUM(t.GiaTri), 0) AS TongTien
    FROM DMTonKho t WHERE t.Nam = @Nam AND (@MaKho = 0 OR t.MaKho = @MaKho);
END;
GO

-- =======================================================
-- 6. DASHBOARD
-- =======================================================
IF OBJECT_ID('sp_GetDashboardStats', 'P') IS NOT NULL
    DROP PROCEDURE sp_GetDashboardStats;
GO

CREATE PROCEDURE sp_GetDashboardStats
    @MaKho INT = 0 
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @NamHienTai INT = YEAR(GETDATE()), @ThangHienTai INT = MONTH(GETDATE());
    DECLARE @TongTonKho INT = (SELECT COUNT(MaMay) FROM DMMay WHERE TonKho = 1 AND (@MaKho = 0 OR MaKho = @MaKho));
    DECLARE @NhapTrongThang INT = (SELECT COUNT(ctn.MaMay) FROM CTPhieuNhap ctn JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu WHERE YEAR(pn.NgayNhap) = @NamHienTai AND MONTH(pn.NgayNhap) = @ThangHienTai AND (@MaKho = 0 OR pn.MaKho = @MaKho));
    DECLARE @XuatTrongThang INT = (SELECT COUNT(ctx.MaMay) FROM CTPhieuXuat ctx JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu WHERE YEAR(px.NgayXuat) = @NamHienTai AND MONTH(px.NgayXuat) = @ThangHienTai AND (@MaKho = 0 OR px.MaKho = @MaKho));
    SELECT @TongTonKho AS TotalStock, @NhapTrongThang AS ImportMonth, @XuatTrongThang AS ExportMonth;
    
    ;WITH ThangTrongNam AS (SELECT 1 AS Thang UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12)
    SELECT t.Thang AS Month, ISNULL(v.SoLuong, 0) AS ImportQty, ISNULL(r.SoLuong, 0) AS ExportQty
    FROM ThangTrongNam t
    LEFT JOIN (SELECT MONTH(pn.NgayNhap) as Thang, COUNT(ctn.MaMay) as SoLuong FROM CTPhieuNhap ctn JOIN PhieuNhap pn ON ctn.SoPhieu = pn.SoPhieu WHERE YEAR(pn.NgayNhap) = @NamHienTai AND (@MaKho = 0 OR pn.MaKho = @MaKho) GROUP BY MONTH(pn.NgayNhap)) v ON t.Thang = v.Thang
    LEFT JOIN (SELECT MONTH(px.NgayXuat) as Thang, COUNT(ctx.MaMay) as SoLuong FROM CTPhieuXuat ctx JOIN PhieuXuat px ON ctx.SoPhieu = px.SoPhieu WHERE YEAR(px.NgayXuat) = @NamHienTai AND (@MaKho = 0 OR px.MaKho = @MaKho) GROUP BY MONTH(px.NgayXuat)) r ON t.Thang = r.Thang
    ORDER BY t.Thang;
END;
GO