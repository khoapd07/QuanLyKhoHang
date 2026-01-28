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
                  <tr v-if="reportData.length === 0">
                    <td colspan="10" class="text-center">Không có dữ liệu.</td>
                  </tr>
                  <tr v-for="(item, index) in reportData" :key="item.maSP">
                    <td data-label="Stt">{{ index + 1 }}</td>
                    <td data-label="Mã SP">{{ item.maSP }}</td>
                    <td data-label="Tên SP">{{ item.tenSP }}</td>
                    <td data-label="ĐVT">{{ item.dvt }}</td>
                    <td data-label="TĐK">{{ item.tonDauKySL }}</td>
                    <td data-label="NTK">{{ item.nhapTrongKySL }}</td>
                    <td data-label="XTK">{{ item.xuatTrongKySL }}</td>
                    <td data-label="TCK"><strong>{{ item.tonCuoiKySL }}</strong></td>
                    <td data-label="Giá/BQ">{{ formatCurrency(item.giaVon) }}</td>
                    <td data-label="Thành Tiền"><strong>{{ formatCurrency(item.tonCuoiKyGT) }}</strong></td>
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
import {
  Document,
  Packer,
  Paragraph,
  Table,
  TableCell,
  TableRow,
  WidthType,
  AlignmentType,
  HeadingLevel,
  TextRun,
  BorderStyle
} from "docx";
import { saveAs } from "file-saver";

// --- CẤU HÌNH ---
const API_URL = 'http://localhost:8080/api/thong-ke/xuat-nhap-ton';

// --- STATE MANAGEMENT ---
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

const reportTitle = computed(() => {
  return currentTenKho.value 
    ? `Kết quả tồn kho: ${currentTenKho.value}` 
    : 'Kết quả tồn kho: Tất cả kho';
});

// --- 3. CÁC HÀM XỬ LÝ ---

// Hàm định dạng tiền tệ
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// Hàm gọi API lấy dữ liệu
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
    console.log("Dữ liệu tải về:", data);
  } catch (error) {
    console.error("Lỗi gọi API:", error);
    alert("Không thể tải báo cáo. Vui lòng kiểm tra Server.");
  } finally {
    loading.value = false;
  }
};

// --- QUAN TRỌNG: Hàm tạo ô bảng (Helper) ---
// (Bạn đang bị thiếu hàm này nên mới báo lỗi "createCell is not defined")
const createCell = (text, isBold = false) => {
  return new TableCell({
    children: [new Paragraph({ 
        children: [new TextRun({ text: text || "", bold: isBold, size: 20 })], // Size 20 = 10pt
        alignment: AlignmentType.CENTER 
    })],
    verticalAlign: "center",
    borders: {
        top: { style: BorderStyle.SINGLE, size: 1, color: "000000" },
        bottom: { style: BorderStyle.SINGLE, size: 1, color: "000000" },
        left: { style: BorderStyle.SINGLE, size: 1, color: "000000" },
        right: { style: BorderStyle.SINGLE, size: 1, color: "000000" },
    }
  });
};

// Hàm xuất file Word
const printToWord = () => {
  // Kiểm tra dữ liệu
  if (!reportData.value || reportData.value.length === 0) {
    alert("Không có dữ liệu để in báo cáo.");
    return;
  }

  try {
    // 1. Chuẩn bị thông tin Header
    const companyName = "CÔNG TY TNHH THƯƠNG MẠI DỊCH VỤ NHẬT TIẾN THANH"; 
    const address = "Địa chỉ: 53 Trần Bình Trọng, Phường 1, Quận 5, TP. Hồ Chí Minh";
    const warehouseName = filters.warehouseId === 0 ? "Tất cả các kho" : (currentTenKho.value || "Kho chưa xác định");
    const dateRange = `Từ ngày: ${filters.startDate} đến ngày: ${filters.endDate}`;

    // 2. Tạo Header Section
    const headerSection = [
      new Paragraph({
        children: [new TextRun({ text: "NTT", bold: true, size: 48, color: "000088" })], // Logo giả
        alignment: AlignmentType.CENTER,
      }),
      new Paragraph({
        children: [new TextRun({ text: companyName, bold: true, size: 28 })],
        alignment: AlignmentType.CENTER,
        spacing: { after: 100 },
      }),
      new Paragraph({
        text: address,
        alignment: AlignmentType.CENTER,
        spacing: { after: 300 },
      }),
      new Paragraph({
        text: "BÁO CÁO XUẤT NHẬP TỒN",
        heading: HeadingLevel.HEADING_1,
        alignment: AlignmentType.CENTER,
        spacing: { after: 100, before: 200 },
      }),
      new Paragraph({
        text: dateRange,
        alignment: AlignmentType.CENTER,
        italics: true,
        spacing: { after: 50 },
      }),
      new Paragraph({
        text: `Kho: ${warehouseName}`,
        alignment: AlignmentType.CENTER,
        italics: true,
        spacing: { after: 200 },
      }),
    ];

    // 3. Tạo Hàng Tiêu Đề Bảng
    const tableHeader = new TableRow({
      tableHeader: true,
      children: [
        createCell("STT", true),
        createCell("Mã SP", true),
        createCell("Tên Sản Phẩm", true),
        createCell("ĐVT", true),
        createCell("Tồn Đầu", true),
        createCell("Nhập", true),
        createCell("Xuất", true),
        createCell("Tồn Cuối", true),
        createCell("Giá BQ", true),
        createCell("Thành Tiền", true),
      ],
    });

    // 4. Tạo Các Hàng Dữ Liệu
    const dataRows = reportData.value.map((item, index) => {
      return new TableRow({
        children: [
          createCell((index + 1).toString()),
          createCell(item.maSP || ""),
          createCell(item.tenSP || ""),
          createCell(item.donvitinh || ""),
          
          createCell((item.tonDau || 0).toString()), 
          createCell((item.nhapTrong || 0).toString()), 
          createCell((item.xuatTrong || 0).toString()), 
          
          createCell((item.tonCuoi || 0).toString(), true),
          
          createCell(formatCurrency(item.giaBQ)), 
          createCell(formatCurrency(item.thanhTien), true), 
        ],
      });
    });

    // 5. Tạo Bảng
    const table = new Table({
      width: { size: 100, type: WidthType.PERCENTAGE },
      rows: [tableHeader, ...dataRows],
    });

    // 6. Tạo Document
    const doc = new Document({
      sections: [{
        properties: {},
        children: [
          ...headerSection,
          table,
          new Paragraph({ text: "", spacing: { before: 300 } }), 
          new Paragraph({ 
            children: [ new TextRun({ text: "Người lập biểu", size: 24, bold: true })],
            alignment: AlignmentType.RIGHT, 
            spacing: { right: 1000, before: 500 } 
          }),
          new Paragraph({ 
            children: [ new TextRun({ text: "(Ký, ghi rõ họ tên)", italics: true, size: 20 })],
            alignment: AlignmentType.RIGHT, 
             spacing: { right: 1100 } 
          }),
        ],
      }],
    });

    // 7. Xuất file
    Packer.toBlob(doc).then((blob) => {
      saveAs(blob, `BaoCao_XNT_${filters.endDate}.docx`);
    });

  } catch (error) {
    console.error("Lỗi xuất file Word:", error);
    alert("Có lỗi khi tạo file Word. F12 để xem chi tiết.");
  }
};

// Giả lập export Excel
const exportReport = () => {
    alert(`Đang xuất Excel...`);
};

// --- LIFECYCLE ---
onMounted(() => {
  fetchInventoryReport();
});

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
