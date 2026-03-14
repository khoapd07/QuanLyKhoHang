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
INSERT INTO TrangThai (TenTrangThai) VALUES 
(N'Chưa xác định'), 
(N'Bình thường'),           -- Bình thường để khoảng trắng
(N'New'), 
(N'Like New'), 
(N'Hỏng'), 
(N'Xác'), 
(N'Thu hồi'),
(N'Nhập Khẩu');

-- 2.3. Tạo Kho & Tài khoản
INSERT INTO Kho (TenKho, DiaChi) VALUES (N'Kho 176 Lê Hồng Phong', N'176 Lê Hồng Phong, TP.HCM');
INSERT INTO Kho (TenKho, DiaChi) VALUES (N'Kho Quận 2', N'Khu công nghệ cao, TP Thủ Đức');

INSERT INTO TaiKhoan (TenTaiKhoan, Password, MaVaitro, MaKho) VALUES  
('admin', '$2a$12$aSiGH8LKYo/YBFwCSeoYROv/CORGKOZ4LYfyNPV8I91fNt.wtOnrC', 1, 1),
('staff', '$2a$12$aSiGH8LKYo/YBFwCSeoYROv/CORGKOZ4LYfyNPV8I91fNt.wtOnrC', 2, 1);

-- 2.4. Đơn Vị Giao Dịch
INSERT INTO DMDonVi (MaDonVi, TenDonVi, LoaiDonVi) VALUES ('NCC_MACDINH', N'Nhà Cung Cấp Mặc Định', 1);
INSERT INTO DMDonVi (MaDonVi, TenDonVi, LoaiDonVi) VALUES ('KH_MACDINH', N'Khách Hàng Mặc Định', 2);


INSERT INTO DMHangSX (TenHang) VALUES (N'Ricoh'), (N'Toshiba'), (N'Konica'), (N'Epson'), (N'Fujitsu'), (N'Sindoh'), (N'Fujifilm'), (N'Dell');
INSERT INTO LoaiSP (TenLoai) VALUES (N'Máy Văn Phòng'), (N'Laptop'), (N'Máy in');


