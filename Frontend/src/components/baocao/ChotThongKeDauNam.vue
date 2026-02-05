<template>
  <div class="content-wrapper">
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0">Quản Lý Chốt Tồn Kho</h1>
          </div>
        </div>
      </div>
    </div>

    <section class="content">
      <div class="container-fluid">

        <div class="card card-primary card-outline card-outline-tabs">
          <div class="card-header p-0 border-bottom-0">
            <ul class="nav nav-tabs" role="tablist">
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'action' }" @click="activeTab = 'action'" href="#"
                  role="tab">
                  <i class="fas fa-edit"></i> Thực Hiện Chốt Sổ
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'" href="#"
                  role="tab">
                  <i class="fas fa-history"></i> Xem Lại Lịch Sử
                </a>
              </li>
            </ul>
          </div>

          <div class="card-body">
            <div class="tab-content">

              <div class="tab-pane fade" :class="{ 'show active': activeTab === 'action' }">
                <div class="alert alert-warning">
                  <i class="fas fa-exclamation-triangle"></i> <b>Lưu ý:</b> Thao tác này sẽ ghi đè dữ liệu tồn đầu/tồn
                  cuối của năm được chọn.
                </div>

                <div class="row">
                  <div class="col-lg-5 col-md-6 col-12">
                    <div class="form-group">
                      <label>Năm Cần Chốt</label>
                      <input type="number" class="form-control" v-model="filters.nam" placeholder="--- Chọn năm cần chốt sổ ---">
                    </div>
                  </div>
                  <div class="col-lg-5 col-md-6 col-12">
                    <div class="form-group">
                      <label>Kho/Chi nhánh áp dụng</label>
                      <select class="form-control" v-model="filters.warehouseId">
                        <option :value="0">Tất cả các kho</option>
                        <option v-for="kho in khoList" :key="kho.maKho" :value="kho.maKho">{{ kho.tenKho }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-lg-2 col-md-12 col-12 d-flex align-items-end">
                    <div class="form-group w-100">
                      <button type="button" class="btn btn-warning btn-block" @click="thucHienChotSo"
                        :disabled="loading">
                        <i class="fas" :class="loading ? 'fa-spinner fa-spin' : 'fa-save'"></i>
                        {{ loading ? 'Đang xử lý...' : 'Chốt Sổ' }}
                      </button>
                    </div>
                  </div>
                </div>

                <div class="mt-4" v-if="reportData.length > 0">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <h5 class="text-success m-0">
                      <i class="fas fa-check-circle"></i> Kết quả vừa chốt: {{ currentTenKho }}
                      <span class="badge badge-secondary ml-2">Tổng: {{ actionPagination.total }} dòng</span>
                    </h5>
                    <button type="button" class="btn btn-sm btn-info"
                      @click="printToWord(filters.nam, filters.warehouseId, currentTenKho)">
                      <i class="fas" :class="isExporting ? 'fa-spinner fa-spin' : 'fa-file-word'"></i>
                      {{ isExporting ? 'Đang xuất...' : 'In File Word' }}
                    </button>
                  </div>

                  <div class="table-responsive" style="max-height: 500px;">
                    <table class="table table-bordered table-striped table-head-fixed text-nowrap">
                      <thead>
                        <tr>
                          <th>Stt</th>
                          <th>Mã SP</th>
                          <th>Tên SP</th>
                          <th>ĐVT</th>
                          <th>Tồn Đầu</th>
                          <th>Nhập</th>
                          <th>Xuất</th>
                          <th>Tồn Cuối</th>
                          <th>Thành Tiền</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(item, index) in reportData" :key="index">
                          <td>{{ item.stt }}</td>
                          <td>{{ item.maSP }}</td>
                          <td>{{ item.tenSP }}</td>
                          <td>{{ item.donvitinh }}</td>
                          <td class="text-right">{{ item.tonDau }}</td>
                          <td class="text-right">{{ item.nhapTrong }}</td>
                          <td class="text-right">{{ item.xuatTrong }}</td>
                          <td class="text-right font-weight-bold">{{ item.tonCuoi }}</td>
                          <td class="text-right font-weight-bold">{{ formatCurrency(item.thanhTien) }}</td>
                        </tr>
                        <tr v-if="reportData.length > 0" class="bg-warning font-weight-bold"
                          style="background-color: #fff3cd !important;">
                          <td colspan="4" class="text-center">TỔNG CỘNG</td>
                          <td class="text-right">{{ grandTotalAction.tdk }}</td>
                          <td class="text-right">{{ grandTotalAction.ntk }}</td>
                          <td class="text-right">{{ grandTotalAction.xtk }}</td>
                          <td class="text-right">{{ grandTotalAction.tck }}</td>
                          <td class="text-right">{{ formatCurrency(grandTotalAction.tien) }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div class="d-flex justify-content-center mt-3 px-3 pb-3" v-if="actionPagination.total > 0">
                    <ul class="pagination pagination-sm m-0">
                      <li class="page-item" :class="{ disabled: actionPagination.page === 0 }">
                        <a class="page-link" href="#" @click.prevent="changeActionPage(actionPagination.page - 1)">« Trước</a>
                      </li>
                      <li v-for="(page, index) in visibleActionPages" :key="index" class="page-item"
                        :class="{ active: page === actionPagination.page + 1, disabled: page === '...' }">
                        <a class="page-link" href="#"
                          @click.prevent="page !== '...' ? changeActionPage(page - 1) : null">{{ page }}</a>
                      </li>
                      <li class="page-item"
                        :class="{ disabled: actionPagination.page >= actionPagination.totalPages - 1 }">
                        <a class="page-link" href="#" @click.prevent="changeActionPage(actionPagination.page + 1)">Sau »</a>
                      </li>
                    </ul>
                  </div>

                </div>
              </div>

              <div class="tab-pane fade" :class="{ 'show active': activeTab === 'history' }">
                <div class="row">
                  <div class="col-lg-5 col-md-6 col-12">
                    <div class="form-group">
                      <label>Xem Năm</label>
                      <input type="number" class="form-control" v-model="historyFilters.nam" placeholder="Nhập năm...">
                    </div>
                  </div>
                  <div class="col-lg-5 col-md-6 col-12">
                    <div class="form-group">
                      <label>Chọn Kho</label>
                      <select class="form-control" v-model="historyFilters.warehouseId">
                        <option :value="0">Tất cả các kho</option>
                        <option v-for="kho in khoList" :key="kho.maKho" :value="kho.maKho">{{ kho.tenKho }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-lg-2 col-md-12 col-12 d-flex align-items-end">
                    <div class="form-group w-100">
                      <button type="button" class="btn btn-primary btn-block" @click="searchHistory"
                        :disabled="loadingHistory">
                        <i class="fas" :class="loadingHistory ? 'fa-spinner fa-spin' : 'fa-search'"></i>
                        {{ loadingHistory ? 'Đang tải...' : 'Xem Dữ Liệu' }}
                      </button>
                    </div>
                  </div>
                </div>

                <div class="mt-4" v-if="historyData.length > 0">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <h5 class="text-primary m-0">
                      <i class="fas fa-list-alt"></i> Dữ liệu lưu trữ: {{ historyTenKho }}
                      <span class="badge badge-secondary ml-2">Tổng: {{ historyPagination.total }} dòng</span>
                    </h5>
                    <button type="button" class="btn btn-sm btn-info"
                      @click="printToWord(historyFilters.nam, historyFilters.warehouseId, historyTenKho)">
                      <i class="fas" :class="isExporting ? 'fa-spinner fa-spin' : 'fa-file-word'"></i>
                      {{ isExporting ? 'Đang xuất...' : 'In File Word' }}
                    </button>
                  </div>

                  <div class="table-responsive" style="max-height: 500px;">
                    <table class="table table-bordered table-striped table-head-fixed text-nowrap">
                      <thead>
                        <tr>
                          <th>Stt</th>
                          <th>Mã SP</th>
                          <th>Tên SP</th>
                          <th>ĐVT</th>
                          <th>Tồn Đầu</th>
                          <th>Nhập</th>
                          <th>Xuất</th>
                          <th>Tồn Cuối</th>
                          <th>Thành Tiền</th>
                        </tr>
                      </thead>

                      <tbody>
                        <tr v-for="(item, index) in historyData" :key="index">
                          <td class="text-center">{{ item.stt }}</td>
                          <td class="text-primary font-weight-bold">{{ item.maSP }}</td>
                          <td>{{ item.tenSP }}</td>
                          <td class="text-center">{{ item.donvitinh }}</td>
                          <td class="text-right">{{ item.tonDau }}</td>
                          <td class="text-right">{{ item.nhapTrong }}</td>
                          <td class="text-right">{{ item.xuatTrong }}</td>
                          <td class="text-right font-weight-bold bg-warning bg-opacity-25">{{ item.tonCuoi }}</td>
                          <td class="text-right font-weight-bold text-primary">{{ formatCurrency(item.thanhTien) }}</td>
                        </tr>
                        <tr v-if="historyData.length > 0" class="bg-primary font-weight-bold"
                          style="background-color: #cce5ff !important; color: #004085;">
                          <td colspan="4" class="text-center">TỔNG CỘNG</td>
                          <td class="text-right">{{ grandTotalHistory.tdk }}</td>
                          <td class="text-right">{{ grandTotalHistory.ntk }}</td>
                          <td class="text-right">{{ grandTotalHistory.xtk }}</td>
                          <td class="text-right">{{ grandTotalHistory.tck }}</td>
                          <td class="text-right">{{ formatCurrency(grandTotalHistory.tien) }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div class="d-flex justify-content-center mt-3 px-3 pb-3" v-if="historyPagination.total > 0">
                    <ul class="pagination pagination-sm m-0">
                      <li class="page-item" :class="{ disabled: historyPagination.page === 0 }">
                        <a class="page-link" href="#" @click.prevent="changeHistoryPage(historyPagination.page - 1)">« Trước</a>
                      </li>
                      <li v-for="(page, index) in visibleHistoryPages" :key="index" class="page-item"
                        :class="{ active: page === historyPagination.page + 1, disabled: page === '...' }">
                        <a class="page-link" href="#"
                          @click.prevent="page !== '...' ? changeHistoryPage(page - 1) : null">{{ page }}</a>
                      </li>
                      <li class="page-item"
                        :class="{ disabled: historyPagination.page >= historyPagination.totalPages - 1 }">
                        <a class="page-link" href="#" @click.prevent="changeHistoryPage(historyPagination.page + 1)">Sau »</a>
                      </li>
                    </ul>
                  </div>

                </div>
                <div v-else-if="searchedHistory" class="text-center mt-5 text-muted">
                  <p><i class="fas fa-search mb-2" style="font-size: 2rem;"></i><br>Không tìm thấy dữ liệu chốt sổ cho
                    năm/kho này.</p>
                </div>

              </div>
            </div>
          </div>
        </div>

      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import api from '@/utils/axios';
import { saveAs } from "file-saver";
import PizZip from "pizzip";
import Docxtemplater from "docxtemplater";

const API_BASE = '/thong-ke';

const activeTab = ref('action');
const khoList = ref([]);
const isExporting = ref(false);

// [FIX] Khai báo rõ ràng 2 biến tổng cho 2 tab
const grandTotalAction = ref({ tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });
const grandTotalHistory = ref({ tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });

// TAB 1 Data
const filters = reactive({ nam: new Date().getFullYear() - 1, warehouseId: 0 });
const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref('');
const actionPagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });

// TAB 2 Data
const historyFilters = reactive({ nam: new Date().getFullYear(), warehouseId: 0 });
const historyData = ref([]);
const loadingHistory = ref(false);
const historyTenKho = ref('');
const searchedHistory = ref(false);
const historyPagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });

// Computed Pagination Action
const visibleActionPages = computed(() => {
  const total = actionPagination.totalPages;
  const current = actionPagination.page + 1;
  const delta = 2;
  const range = [], rangeWithDots = [];
  let l;
  for (let i = 1; i <= total; i++) {
    if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
  }
  for (let i of range) {
    if (l) {
      if (i - l === 2) rangeWithDots.push(l + 1);
      else if (i - l !== 1) rangeWithDots.push('...');
    }
    rangeWithDots.push(i);
    l = i;
  }
  return rangeWithDots;
});

// Computed Pagination History
const visibleHistoryPages = computed(() => {
  const total = historyPagination.totalPages;
  const current = historyPagination.page + 1;
  const delta = 2;
  const range = [], rangeWithDots = [];
  let l;
  for (let i = 1; i <= total; i++) {
    if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
  }
  for (let i of range) {
    if (l) {
      if (i - l === 2) rangeWithDots.push(l + 1);
      else if (i - l !== 1) rangeWithDots.push('...');
    }
    rangeWithDots.push(i);
    l = i;
  }
  return rangeWithDots;
});

const formatCurrency = (value) => {
  if (value === null || value === undefined || isNaN(value)) return '0';
  return new Intl.NumberFormat('vi-VN', { style: 'decimal', currency: 'VND' }).format(value);
};

// --- LOGIC API ---

// 1. Chốt Sổ (Tab 1)
const thucHienChotSo = async () => {
  if (!confirm(`Bạn có chắc chắn muốn chốt sổ cho năm ${filters.nam}? Dữ liệu cũ sẽ bị đè!`)) return;
  actionPagination.page = 0;
  goiApiChotSo();
};

const changeActionPage = (newPage) => {
  if (newPage < 0 || newPage >= actionPagination.totalPages) return;
  actionPagination.page = newPage;
  goiApiXemKetQuaSauChot();
};

const goiApiChotSo = async () => {
  loading.value = true;
  reportData.value = [];
  try {
    const response = await api.post(`${API_BASE}/chot-so`, null, {
      params: { nam: filters.nam, maKho: filters.warehouseId }
    });
    const data = response.data;
    currentTenKho.value = data.tenKho;
    reportData.value = data.danhSachChiTiet;
    actionPagination.page = data.currentPage;
    actionPagination.total = data.totalItems;
    actionPagination.totalPages = data.totalPages;
    
    // [FIX] Cập nhật đúng biến cho Tab 1
    if (data.grandTotal) grandTotalAction.value = data.grandTotal;
    
    alert("Chốt sổ thành công!");
  } catch (error) {
    const msg = error.response?.data?.message || error.message;
    alert("Lỗi khi chốt sổ: " + msg);
  } finally {
    loading.value = false;
  }
};

const goiApiXemKetQuaSauChot = async () => {
  loading.value = true;
  try {
    const namKetQua = parseInt(filters.nam) + 1;
    const response = await api.get(`${API_BASE}/lich-su`, {
      params: {
        nam: namKetQua,
        maKho: filters.warehouseId,
        page: actionPagination.page,
        size: actionPagination.size
      }
    });
    const data = response.data;
    reportData.value = data.danhSachChiTiet || [];
    actionPagination.page = data.currentPage;
    actionPagination.total = data.totalItems;
    actionPagination.totalPages = data.totalPages;
    
    // [FIX] Cập nhật đúng biến cho Tab 1
    if (data.grandTotal) grandTotalAction.value = data.grandTotal;
    
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

// 2. Xem Lịch Sử (Tab 2)
const searchHistory = () => {
  historyPagination.page = 0;
  xemLichSu();
};

const changeHistoryPage = (newPage) => {
  if (newPage < 0 || newPage >= historyPagination.totalPages) return;
  historyPagination.page = newPage;
  xemLichSu();
};

const xemLichSu = async () => {
  loadingHistory.value = true;
  historyData.value = [];
  searchedHistory.value = false;

  try {
    const response = await api.get(`${API_BASE}/lich-su`, {
      params: {
        nam: historyFilters.nam,
        maKho: historyFilters.warehouseId,
        page: historyPagination.page,
        size: historyPagination.size
      }
    });

    const data = response.data;
    historyTenKho.value = data.tenKho;
    historyData.value = data.danhSachChiTiet || [];
    historyPagination.page = data.currentPage;
    historyPagination.total = data.totalItems;
    historyPagination.totalPages = data.totalPages;

    searchedHistory.value = true;
    
    // [FIX] Cập nhật đúng biến cho Tab 2
    if (data.grandTotal) grandTotalHistory.value = data.grandTotal;
    
  } catch (error) {
    console.error("Lỗi tải lịch sử:", error);
    alert("Lỗi: " + (error.response?.data?.message || "Lỗi hệ thống"));
  } finally {
    loadingHistory.value = false;
  }
}

// 3. In Word (Logic giữ nguyên)
const loadFile = async (url) => {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`Không thể tải file mẫu: ${url}`);
  return await response.arrayBuffer();
};

const printToWord = async (namInput, maKhoInput, tenKhoString) => {
  if (isExporting.value) return;
  isExporting.value = true;
  try {
    let namCanLay = namInput;
    if (activeTab.value === 'action') {
      namCanLay = parseInt(namInput) + 1;
    }
    const response = await api.get(`${API_BASE}/lich-su`, {
      params: { nam: namCanLay, maKho: maKhoInput, page: 0, size: 999999 }
    });
    const sourceData = response.data.danhSachChiTiet || [];
    if (sourceData.length === 0) {
      alert("Không có dữ liệu để in."); return;
    }
    const content = await loadFile("/File_Mau_BaoCaoChotSoNam.docx");
    const zip = new PizZip(content);
    const doc = new Docxtemplater(zip, { paragraphLoop: true, linebreaks: true });
    
    const totals = sourceData.reduce((acc, item) => {
      acc.tdk += item.tonDau || 0; acc.ntk += item.nhapTrong || 0;
      acc.xtk += item.xuatTrong || 0; acc.tck += item.tonCuoi || 0;
      acc.tien += item.thanhTien || 0; return acc;
    }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });

    const today = new Date();
    const dataToRender = {
      nam: namCanLay, tenKho: tenKhoString || "Kho chưa xác định",
      d: String(today.getDate()).padStart(2, '0'), m: String(today.getMonth() + 1).padStart(2, '0'), y: today.getFullYear(),
      sumTDK: formatCurrency(totals.tdk), sumNTK: formatCurrency(totals.ntk),
      sumXTK: formatCurrency(totals.xtk), sumTCK: formatCurrency(totals.tck),
      sumTien: formatCurrency(totals.tien),
      p: sourceData.map((item, index) => ({
        stt: index + 1, ma: item.maSP || "", ten: item.tenSP || "", dvt: item.donvitinh || "",
        tdk: item.tonDau || 0, ntk: item.nhapTrong || 0, xtk: item.xuatTrong || 0,
        tck: item.tonCuoi || 0, gia: formatCurrency(item.giaBQ), tien: formatCurrency(item.thanhTien)
      }))
    };
    doc.render(dataToRender);
    const out = doc.getZip().generate({ type: "blob", mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document" });
    saveAs(out, `BienBan_${tenKhoString}_${namCanLay}.docx`);
  } catch (error) {
    console.error("Lỗi in Word:", error); alert("Lỗi xuất file: " + error.message);
  } finally {
    isExporting.value = false;
  }
};

const loadKho = async () => {
  try {
    const res = await api.get('/kho');
    khoList.value = res.data;
    if (res.data.length > 0) {
      filters.warehouseId = res.data[0].maKho;
      historyFilters.warehouseId = res.data[0].maKho;
    }
  } catch (e) { console.error("Lỗi tải kho:", e); }
};

onMounted(() => loadKho());
</script>

<style scoped>
/* STYLE GIỮ NGUYÊN NHƯ CŨ */
.card-primary.card-outline-tabs>.card-header a.active { border-top: 3px solid #ffc107; }
.nav-link, .page-link { cursor: pointer; font-weight: 600; }
.page-item.active .page-link { background-color: #007bff; border-color: #007bff; color: white; }
.page-item.disabled .page-link { pointer-events: none; background-color: #fff; color: #6c757d; }
.form-control-sm { height: calc(1.8125rem + 2px); font-size: .875rem; }
.table-container { max-height: 65vh; overflow: auto; position: relative; border-top: 1px solid #dee2e6; scrollbar-width: thin; }
.table-container::-webkit-scrollbar { width: 6px; height: 6px; }
.table-container::-webkit-scrollbar-thumb { background: #c1c1c1; border-radius: 4px; }
.sticky-thead th { position: sticky; top: 0; background-color: #f8f9fa; z-index: 10; box-shadow: 0 1px 2px rgba(0,0,0,0.1); white-space: nowrap; }
@media (max-width: 768px) {
  .nav-tabs .nav-link { padding: 0.5rem 0.8rem; font-size: 0.9rem; }
  .col-6 { padding-left: 5px; padding-right: 5px; } 
  .table th, .table td { padding: 8px 6px; font-size: 13px; }
}
</style>