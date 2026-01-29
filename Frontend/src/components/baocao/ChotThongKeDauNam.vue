<template>
  <div class="content-wrapper">
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6"><h1 class="m-0">Chốt Tồn Kho Đầu Năm</h1></div>
        </div>
      </div>
    </div>

    <section class="content">
      <div class="container-fluid">
        <div class="card card-warning card-outline">
          <div class="card-header">
            <h3 class="card-title"><i class="fas fa-cogs"></i> Cấu hình chốt sổ</h3>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-lg-6 col-md-6 col-12">
                <div class="form-group">
                  <label>Chọn Năm Cần Chốt</label>
                  <input type="number" class="form-control" v-model="filters.nam" placeholder="Ví dụ: 2026">
                </div>
              </div>
              
              <div class="col-lg-6 col-md-6 col-12">
                <div class="form-group">
                  <label>Kho/Chi nhánh áp dụng</label>
                  <select class="form-control" v-model="filters.warehouseId">
                    <option v-for="kho in khoList" :key="kho.id" :value="kho.id">{{ kho.name }}</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          
          <div class="card-footer">
            <button type="button" class="btn btn-warning" @click="thucHienChotSo" :disabled="loading">
              <i class="fas fa-save"></i> {{ loading ? 'Đang xử lý...' : 'Thực Hiện Chốt Sổ' }}
            </button>
            
            <button v-if="reportData.length > 0" type="button" class="btn btn-info float-right" @click="printToWord">
              <i class="fas fa-file-word"></i> In Biên Bản Chốt
            </button>
          </div>
        </div>

        <div class="card mt-4" v-if="reportData.length > 0">
          <div class="card-header">
            <h3 class="card-title text-success"><i class="fas fa-check-circle"></i> {{ reportTitle }}</h3>
          </div>
          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped">
              <thead>
                <tr>
                  <th style="width: 10px">Stt</th>
                  <th>Mã SP</th>
                  <th>Tên Sản Phẩm</th>
                  <th>ĐVT</th>
                  <th>Tồn Đầu</th>
                  <th>Nhập</th>
                  <th>Xuất</th>
                  <th>Tồn Cuối</th>
                  <th>Giá BQ</th>
                  <th>Thành Tiền</th>
                </tr>
              </thead>
              <tbody>
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
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import axios from 'axios';
import { Document, Packer, Paragraph, Table, TableCell, TableRow, WidthType, AlignmentType, HeadingLevel, TextRun, BorderStyle } from "docx";
import { saveAs } from "file-saver";

// --- CẤU HÌNH API ---
// Lưu ý: Đổi URL sang endpoint chốt sổ nếu cần, ở đây dùng chung controller
const API_BASE = 'http://localhost:8080/api/thong-ke'; 

// --- STATE ---
const filters = reactive({
  nam: new Date().getFullYear() + 1, // Mặc định năm sau (ví dụ đang 2025 thì chốt cho 2026)
  warehouseId: 1, // Mặc định kho 1
});

const khoList = ref([
    { id: 1, name: 'Kho Tổng Hà Nội' },
    { id: 2, name: 'Kho Chi Nhánh HCM' }
]);

const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('');

const reportTitle = computed(() => `Kết quả chốt sổ năm ${filters.nam}: ${currentTenKho.value}`);

// --- HELPER ---
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// --- API: CHỐT SỔ (POST) ---
const thucHienChotSo = async () => {
  if(!confirm(`Bạn có chắc chắn muốn chốt sổ cho năm ${filters.nam} tại kho này không? Dữ liệu cũ của năm này sẽ bị ghi đè!`)) {
      return;
  }

  loading.value = true;
  reportData.value = []; // Reset bảng

  try {
    // Gọi API POST /chot-so
    const response = await axios.post(`${API_BASE}/chot-so`, null, {
      params: {
        nam: filters.nam,
        maKho: filters.warehouseId
      }
    });

    // Backend trả về DTO: { tenKho: "...", danhSachChiTiet: [...] }
    const data = response.data;
    
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet;
    
    alert("Chốt sổ thành công!");

  } catch (error) {
    console.error("Lỗi:", error);
    alert("Lỗi khi chốt sổ: " + (error.response?.data || error.message));
  } finally {
    loading.value = false;
  }
};

// --- WORD EXPORT HELPERS ---
const createCell = (value, isBold = false) => {
  let textToDisplay = "0";
  if (value !== null && value !== undefined) textToDisplay = value.toString();
  else if (typeof value === 'string') textToDisplay = value;
  if(value === "") textToDisplay = "";

  return new TableCell({
    children: [new Paragraph({ 
        children: [new TextRun({ text: textToDisplay, bold: isBold, size: 20, font: "Times New Roman" })], 
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

// --- IN WORD (BIÊN BẢN) ---
const printToWord = () => {
  if (!reportData.value || reportData.value.length === 0) return;

  try {
    const companyName = "CÔNG TY TNHH THƯƠNG MẠI DỊCH VỤ THIẾT BỊ Y TẾ VÀ MÁY VĂN PHÒNG NHẬT TIẾN THANH";
    const address = "Đ/c: 53 Trần Bình Trọng, Phường 1, Quận 5, TP.HCM, Việt Nam";
    const warehouseInfo = `Kho: ${currentTenKho.value}`;

    const headerSection = [
      new Paragraph({ children: [new TextRun({ text: "NTT", bold: true, size: 48, color: "000088", font: "Times New Roman" })], alignment: AlignmentType.CENTER }),
      
      new Paragraph({ 
        children: [new TextRun({ text: companyName, bold: true, size: 26, font: "Times New Roman", allCaps: true })], 
        alignment: AlignmentType.CENTER, spacing: { after: 100 }
      }),

      new Paragraph({ children: [new TextRun({ text: address, size: 22, font: "Times New Roman" })], alignment: AlignmentType.CENTER, spacing: { after: 300 } }),

      // Tiêu đề: BÁO CÁO TỒN KHO ĐẦU NĂM
      new Paragraph({ 
        children: [new TextRun({ text: `BÁO CÁO TỒN KHO ĐẦU NĂM ${filters.nam}`, bold: true, size: 32, font: "Times New Roman" })],
        alignment: AlignmentType.CENTER, spacing: { after: 100, before: 200 },
      }),

      // Chỉ hiển thị Năm
      new Paragraph({
        children: [new TextRun({ text: `Năm tài chính: ${filters.nam}`, bold: true, font: "Times New Roman", size: 24 })],
        alignment: AlignmentType.CENTER, spacing: { after: 300 }
      }),

      // Kho: Canh trái, Đậm, Nghiêng
      new Paragraph({ 
        children: [new TextRun({ text: warehouseInfo, bold: true, italics: true, size: 24, font: "Times New Roman" })], 
        alignment: AlignmentType.LEFT, spacing: { after: 100 } 
      }),
    ];

    const tableHeader = new TableRow({
      tableHeader: true,
      children: [
        createCell("STT", true), createCell("Mã SP", true), createCell("Tên SP", true),
        createCell("ĐVT", true), createCell("Tồn Đầu", true), createCell("Nhập", true),
        createCell("Xuất", true), createCell("Tồn Cuối", true), createCell("Giá BQ", true),
        createCell("Thành Tiền", true),
      ],
    });

    const dataRows = reportData.value.map((item, index) => {
      return new TableRow({
        children: [
          createCell(index + 1),
          createCell(item.maSP || ""),
          createCell(item.tenSP || ""),
          createCell(item.donvitinh || ""),
          createCell(item.tonDau),
          createCell(item.nhapTrong),
          createCell(item.xuatTrong),
          createCell(item.tonCuoi, true),
          createCell(formatCurrency(item.giaBQ)),
          createCell(formatCurrency(item.thanhTien), true),
        ],
      });
    });

    const doc = new Document({
      sections: [{
        children: [...headerSection, new Table({ width: { size: 100, type: WidthType.PERCENTAGE }, rows: [tableHeader, ...dataRows] })],
      }],
    });

    Packer.toBlob(doc).then((blob) => {
      saveAs(blob, `BienBanChotSo_${filters.nam}.docx`);
    });

  } catch (error) {
    console.error(error);
    alert("Lỗi tạo file Word!");
  }
};
</script>

<style scoped>
/* CSS cho đẹp form */
.card-warning.card-outline {
  border-top: 3px solid #ffc107;
}
</style>