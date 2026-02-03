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
                    <option :value="0">Tất cả các kho (Tổng hợp)</option>
                    <option v-for="kho in khoList" :key="kho.maKho" :value="kho.maKho">{{ kho.tenKho }}</option>
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
            <h3 class="card-title text-success"><i class="fas fa-check-circle"></i> Kết quả chốt sổ: {{ currentTenKho }}</h3>
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
import { ref, reactive, onMounted } from 'vue';
// [QUAN TRỌNG] Dùng api từ utils
import api from '@/utils/axios';
import { saveAs } from "file-saver";
import PizZip from "pizzip";
import Docxtemplater from "docxtemplater";

// --- CẤU HÌNH API ---
const API_BASE = '/thong-ke'; 

// --- STATE ---
const filters = reactive({
  nam: new Date().getFullYear() - 1, // Mặc định năm trước để chốt sang năm nay
  warehouseId: 0, 
});

const khoList = ref([]);
const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('');

// --- LOAD DATA ---
const loadKho = async () => {
    try {
        // API lấy danh sách kho: /api/kho
        const res = await api.get('/kho');
        khoList.value = res.data;
        // Nếu có kho thì set mặc định cái đầu tiên (hoặc để 0 là tất cả)
        if (res.data.length > 0) filters.warehouseId = res.data[0].maKho;
    } catch (e) {
        console.error("Lỗi tải kho:", e);
    }
};

// --- HELPER FUNCTIONS ---

const loadFile = async (url) => {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Không thể tải file mẫu: ${url}`);
    }
    return await response.arrayBuffer();
};

const formatCurrency = (value) => {
  if (value === null || value === undefined || isNaN(value)) return '0';
  return new Intl.NumberFormat('vi-VN').format(value);
};

// --- API: CHỐT SỔ (POST) ---
const thucHienChotSo = async () => {
  if(!confirm(`Bạn có chắc chắn muốn chốt sổ cho năm ${filters.nam} tại kho này không? Dữ liệu cũ của năm này sẽ bị ghi đè!`)) {
      return;
  }

  loading.value = true;
  reportData.value = []; 

  try {
    // API: POST /api/thong-ke/chot-so
    // Backend @RequestParam: nam, maKho
    const response = await api.post(`${API_BASE}/chot-so`, null, {
      params: {
        nam: filters.nam,
        maKho: filters.warehouseId
      }
    });

    const data = response.data;
    // Backend trả về: { tenKho: "...", danhSachChiTiet: [...] } (BaoCaoResponseDTO)
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet;
    
    alert("Chốt sổ thành công!");

  } catch (error) {
    console.error("Lỗi:", error);
    const msg = error.response?.data?.message || error.response?.data || error.message;
    alert("Lỗi khi chốt sổ: " + msg);
  } finally {
    loading.value = false;
  }
};

// --- IN WORD (DÙNG TEMPLATE) ---
const printToWord = async () => {
  if (!reportData.value || reportData.value.length === 0) {
    alert("Không có dữ liệu để in.");
    return;
  }

  try {
      const content = await loadFile("/File_Mau_BaoCaoChotSoNam.docx");
      const zip = new PizZip(content);
      const doc = new Docxtemplater(zip, {
          paragraphLoop: true,
          linebreaks: true,
      });

      // Tính tổng
      const totals = reportData.value.reduce((acc, item) => {
          acc.tdk += item.tonDau || 0;
          acc.ntk += item.nhapTrong || 0;
          acc.xtk += item.xuatTrong || 0;
          acc.tck += item.tonCuoi || 0;
          acc.tien += item.thanhTien || 0;
          return acc;
      }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });

      // Ngày giờ
      const today = new Date();
      const dd = String(today.getDate()).padStart(2, '0');
      const mm = String(today.getMonth() + 1).padStart(2, '0');
      const yyyy = today.getFullYear();
      let hours = today.getHours();
      const minutes = String(today.getMinutes()).padStart(2, '0');
      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12;
      hours = hours ? hours : 12; 
      const strHours = String(hours).padStart(2, '0');

      // Map dữ liệu
      const dataToRender = {
          nam: filters.nam,
          tenKho: currentTenKho.value || "Kho chưa xác định",
          
          d: dd, m: mm, y: yyyy, h: strHours, ph: minutes, ampm: ampm,

          sumTDK: formatCurrency(totals.tdk),
          sumNTK: formatCurrency(totals.ntk),
          sumXTK: formatCurrency(totals.xtk),
          sumTCK: formatCurrency(totals.tck),
          sumTien: formatCurrency(totals.tien),

          p: reportData.value.map((item, index) => ({
              stt: index + 1,
              ma: item.maSP || "",
              ten: item.tenSP || "",
              dvt: item.donvitinh || "",
              tdk: item.tonDau || 0,
              ntk: item.nhapTrong || 0,
              xtk: item.xuatTrong || 0,
              tck: item.tonCuoi || 0,
              gia: formatCurrency(item.giaBQ),
              tien: formatCurrency(item.thanhTien)
          }))
      };

      doc.render(dataToRender);

      const out = doc.getZip().generate({
          type: "blob",
          mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      });
      
      saveAs(out, `BienBanChotSo_${filters.nam}.docx`);

  } catch (error) {
      console.error("Lỗi in Word:", error);
      alert("Lỗi xuất file: " + error.message);
  }
};

onMounted(() => loadKho());
</script>

<style scoped>
.card-warning.card-outline {
  border-top: 3px solid #ffc107;
}
</style>