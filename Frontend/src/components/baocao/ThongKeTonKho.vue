<template>
  <div class="content-wrapper">
    <!-- Content Header (Page header) -->
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0">Báo Cáo Xuất Nhập Tồn</h1>
          </div>
          <div class="col-sm-6">
          </div>
        </div>
      </div>
    </div>
    <!-- /.content-header -->

    <!-- Main content -->
    <section class="content">
      <div class="container-fluid">
        <!-- Filter Card -->
        <div class="card card-primary card-outline">
          <div class="card-header">
            <h3 class="card-title">
              <i class="fas fa-filter"></i>
              Bộ lọc báo cáo
            </h3>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-lg-4 col-md-6 col-12">
                <div class="form-group">
                  <label for="startDate">Từ ngày</label>
                  <input type="date" class="form-control" id="startDate" v-model="filters.startDate">
                </div>
              </div>
              <div class="col-lg-4 col-md-6 col-12">
                <div class="form-group">
                  <label for="endDate">Đến ngày</label>
                  <input type="date" class="form-control" id="endDate" v-model="filters.endDate">
                </div>
              </div>
              <div class="col-lg-4 col-md-6 col-12">
                <div class="form-group">
                  <label for="warehouse">Kho/Chi nhánh</label>
                  <select class="form-control" id="warehouse" v-model="filters.warehouseId">
                    <option value="0">Tất cả kho</option>
                    <option v-for="kho in khoList" :key="kho.id" :value="kho.id">{{ kho.name }}</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <button type="button" class="btn btn-primary" @click="fetchInventoryReport">
              <i class="fas fa-search"></i> Lọc báo cáo
            </button>
            <button type="button" class="btn btn-info mr-2" @click="printToWord">
        <i class="fas fa-file-word"></i> In Báo Cáo
      </button>
          </div>
        </div>
        <!-- /.card -->

        <!-- Report Results Card -->
        <div class="card mt-4">
          <div class="card-header">
            <h3 class="card-title">{{ reportTitle }}</h3>
          </div>
          <!-- /.card-header -->
          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped table-responsive-stack">
              <thead>
                <tr>
                  <th style="width: 10px">Stt</th>
                  <th>Mã Sản Phẩm</th>
                  <th>Tên Sản Phẩm</th>
                  <th>ĐVT</th>
                  <th>TĐK</th>
                  <th>NTK</th>
                  <th>XTK</th>
                  <th>TCK</th>
                  <th>Giá/BQ</th>
                  <th>Thành Tiền</th>
                </tr>
              </thead>
              <tbody>
                <!-- Skeleton Loader -->
                <template v-if="loading">
                  <tr v-for="n in 5" :key="`skel-${n}`">
                    <td v-for="m in 10" :key="m"><div class="skeleton-loader"></div></td>
                  </tr>
                </template>

                <!-- Data Rows -->
                <template v-else>
                  <tr v-if="!reportData || reportData.length === 0">
                    <td colspan="10" class="text-center">Không có dữ liệu.</td>
                  </tr>
                  <tr v-for="(item, index) in reportData" :key="index">
                    <td>{{ index + 1 }}</td>
                    <td>{{ item.maSP }}</td>
                    <td>{{ item.tenSP }}</td>
                    <td>{{ item.donvitinh }}</td>
                    
                    <td class="text-right">{{ item.tonDau }}</td>
                    <td class="text-right">{{ item.nhapTrong }}</td>
                    <td class="text-right">{{ item.xuatTrong }}</td>
                    
                    <td class="text-right"><strong>{{ item.tonCuoi }}</strong></td>
                    
                    <td class="text-right">{{ formatCurrency(item.giaBQ) }}</td>
                    <td class="text-right"><strong>{{ formatCurrency(item.thanhTien) }}</strong></td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
          <!-- /.card-body -->
        </div>
        <!-- /.card -->

      </div>
    </section>
    <!-- /.content -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import axios from 'axios';
import { Document, Packer, Paragraph, Table, TableCell, TableRow, WidthType, AlignmentType, HeadingLevel, TextRun, BorderStyle } from "docx";
import { saveAs } from "file-saver";

// --- CẤU HÌNH ---
const API_URL = 'http://localhost:8080/api/thong-ke/xuat-nhap-ton';

// --- STATE MANAGEMENT ---
// --- STATE ---
const filters = reactive({
  startDate: new Date('2024-01-01').toISOString().substr(0, 10),
  endDate: new Date().toISOString().substr(0, 10),
  warehouseId: 0,
});

const khoList = ref([
    { id: 1, name: 'Kho Tổng Hà Nội' },
    { id: 2, name: 'Kho Chi Nhánh HCM' }
]);

const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('');

const reportTitle = computed(() => currentTenKho.value ? `Kết quả: ${currentTenKho.value}` : 'Kết quả tồn kho');

// --- HELPER ---
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// --- FETCH API ---
const fetchInventoryReport = async () => {
  loading.value = true;
  reportData.value = [];
  try {
    const response = await axios.get(API_URL, {
      params: {
        maKho: filters.warehouseId,
        tuNgay: filters.startDate,
        denNgay: filters.endDate,
        loaiLoc: 0
      }
    });
    const data = response.data;
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet;
  } catch (error) {
    console.error("Lỗi:", error);
    alert("Lỗi tải dữ liệu!");
  } finally {
    loading.value = false;
  }
};

// --- HÀM TẠO Ô WORD (Đã fix lỗi hiển thị số) ---
const createCell = (value, isBold = false) => {
  let textToDisplay = "0"; // Mặc định là 0
  if (value !== null && value !== undefined) {
      textToDisplay = value.toString(); // Ép kiểu chuỗi để tránh lỗi
  } else if (typeof value === 'string') {
      textToDisplay = value;
  }
  
  // Nếu giá trị rỗng thì để trống
  if(value === "") textToDisplay = "";

  return new TableCell({
    children: [new Paragraph({ 
        children: [new TextRun({ text: textToDisplay, bold: isBold, size: 16 })], 
        alignment: AlignmentType.CENTER 
    })],
    verticalAlign: "center",
    borders: {
      top: { style: BorderStyle.SINGLE, size: 1 },
      bottom: { style: BorderStyle.SINGLE, size: 1 },
      left: { style: BorderStyle.SINGLE, size: 1 },
      right: { style: BorderStyle.SINGLE, size: 1 },
    }
  });
};

// --- IN WORD (Đã sửa tên biến khớp API) ---
const printToWord = () => {
  if (!reportData.value || reportData.value.length === 0) {
    alert("Không có dữ liệu.");
    return;
  }

  try {
    const companyName = "CÔNG TY TNHH THƯƠNG MẠI DỊCH VỤ THIẾT BỊ Y TẾ VÀ MÁY VĂN PHÒNG NHẬT TIẾN THANH";
    const address = "Đ/c: 53 Trần Bình Trọng, Phường 1, Quận 5, TP.HCM, Việt Nam";
    const warehouseInfo = `Kho: ${currentTenKho.value || "Tất cả"}`; // Lấy tên kho
    const SetDateVN = (dateString) => {
    if (!dateString) return "...";
    // Cắt chuỗi YYYY-MM-DD để tránh lỗi múi giờ
    const [year, month, day] = dateString.split('-'); 
    return ` ${day}/${month}/${year}`;
  };

    // 2. Header (Đã sửa theo yêu cầu)
    const headerSection = [
      // Logo NTT
      new Paragraph({ 
        children: [new TextRun({ text: "NULL", bold: true, size: 48, color: "000088", font: "Times New Roman" })], 
        alignment: AlignmentType.CENTER 
      }),

      // Tên Công Ty (IN ĐẬM)
      new Paragraph({ 
        children: [new TextRun({ 
            text: companyName, 
            bold: true, // <--- In đậm
            size: 18,   // Cỡ chữ to (13pt)
            font: "Times New Roman",
            allCaps: true // Viết hoa toàn bộ cho đẹp
        })], 
        alignment: AlignmentType.CENTER,
        spacing: { after: 100 }
      }),
// địa chỉ
      new Paragraph({ 
        children: [new TextRun({ text: address, size: 18, font: "Times New Roman" })],
        alignment: AlignmentType.CENTER, 
        spacing: { after: 10 } 
      }),

      // dấu thanh ngang
      new Paragraph({ 
        children: [new TextRun({ text: "_________________________________________________________________________________", size: 20,bold: true, font: "Times New Roman" })],
        alignment: AlignmentType.CENTER, 
        spacing: { after: 200 } 
      }),

      // Tiêu đề Báo Cáo
      new Paragraph({ 
        children: [new TextRun({ text: "BÁO CÁO XUẤT NHẬP TỒN", bold: true, size: 32, font: "Times New Roman" })],
        alignment: AlignmentType.CENTER,
        spacing: { after: 100, before: 200 },
      }),

      // 1. Ngày bắt đầu
      new Paragraph({
        children: [
            // Phần tiêu đề: IN ĐẬM
            new TextRun({
                text: "Ngày bắt đầu: ",
                bold: true,
                font: "Times New Roman",
                size: 18 // 11pt
            }),
            // Phần ngày tháng: IN THƯỜNG
            new TextRun({
                text: SetDateVN(filters.startDate),
                bold: false, 
                font: "Times New Roman",
                size: 18
            })
        ],
        alignment: AlignmentType.CENTER,
        spacing: { after: 50 }
      }),

      // 2. Ngày kết thúc
      new Paragraph({
        children: [
            // Phần tiêu đề: IN ĐẬM
            new TextRun({
                text: "Ngày kết thúc: ",
                bold: true,
                font: "Times New Roman",
                size: 18
            }),
            // Phần ngày tháng: IN THƯỜNG
            new TextRun({
                text: SetDateVN(filters.endDate),
                bold: false,
                font: "Times New Roman",
                size: 18
            })
        ],
        alignment: AlignmentType.CENTER,
        spacing: { after: 300 }
      }),

      // Dòng Kho: CANH TRÁI + IN ĐẬM + IN NGHIÊNG
      new Paragraph({ 
        children: [new TextRun({ 
            text: warehouseInfo, 
            bold: true,    // <--- In đậm
            italics: true, // <--- In nghiêng
            size: 14,
            font: "Times New Roman"
        })], 
        alignment: AlignmentType.LEFT, // <--- Canh trái
        spacing: { after: 100 } 
      }),
    ];

    // 2. Table Header
    const tableHeader = new TableRow({
      tableHeader: true,
      children: [
        createCell("STT", true), createCell("Mã SP", true), createCell("Tên SP", true),
        createCell("ĐVT", true), createCell("Tồn Đầu", true), createCell("Nhập", true),
        createCell("Xuất", true), createCell("Tồn Cuối", true), createCell("Giá BQ", true),
        createCell("Thành Tiền", true),
      ],
    });

    // 3. Data Rows (QUAN TRỌNG: Đã sửa tên biến tại đây)
    const dataRows = reportData.value.map((item, index) => {
      return new TableRow({
        children: [
          createCell(index + 1),
          createCell(item.maSP || ""),
          createCell(item.tenSP || ""),
          createCell(item.donvitinh || ""), // Sửa từ dvt -> donvitinh
          createCell(item.tonDau),          // Sửa từ tonDauKySL -> tonDau
          createCell(item.nhapTrong),       // Sửa từ nhapTrongKySL -> nhapTrong
          createCell(item.xuatTrong),       // Sửa từ xuatTrongKySL -> xuatTrong
          createCell(item.tonCuoi, true),   // Sửa từ tonCuoiKySL -> tonCuoi
          createCell(formatCurrency(item.giaBQ)), // Sửa từ giaVon -> giaBQ
          createCell(formatCurrency(item.thanhTien), true), // Sửa từ tonCuoiKyGT -> thanhTien
        ],
      });
    });

    // 4. Build Doc
    const doc = new Document({
      sections: [{
        children: [...headerSection, new Table({ width: { size: 100, type: WidthType.PERCENTAGE }, rows: [tableHeader, ...dataRows] })],
      }],
    });

    Packer.toBlob(doc).then((blob) => {
      saveAs(blob, `BaoCao_${filters.endDate}.docx`);
    });

  } catch (error) {
    console.error(error);
    alert("Lỗi tạo file Word!");
  }
};

onMounted(() => fetchInventoryReport());
</script>

<style scoped>
/* Thêm một chút khoảng cách cho các nút trong card footer */
.card-footer .btn {
  margin-right: 5px;
}

/* Skeleton Loader CSS */
.skeleton-loader {
  width: 100%;
  height: 1.2em; /* Chiều cao tương đương dòng chữ */
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* Responsive Table CSS for Mobile */
@media screen and (max-width: 768px) {
  .table-responsive-stack thead {
    display: none;
  }
  .table-responsive-stack tr {
    display: block;
    margin-bottom: 1rem;
    border: 1px solid #dee2e6;
  }
  .table-responsive-stack td {
    display: block;
    text-align: right;
    border: none;
    border-bottom: 1px solid #dee2e6;
  }
  .table-responsive-stack td::before {
    content: attr(data-label);
    float: left;
    font-weight: bold;
    text-transform: uppercase;
  }
  .table-responsive-stack td:last-child {
    border-bottom: 0;
  }
  
}

</style>
