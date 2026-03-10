<template>
  <div class="content-wrapper">
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6 d-flex align-items-center">
            <h1 class="m-0">Quản Lý Chốt Tồn Kho</h1>
            <span v-if="isChotSoState === false || isChotSoState === 'false'" class="text-danger ml-2"
              style="cursor: pointer; font-size: 2rem;" title="Cảnh báo: Chưa chốt sổ đầu kỳ!"
              @click="showFirstYearWarning">
              <i class="fas fa-exclamation-circle">!</i>
            </span>
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
                <a class="nav-link" :class="{ active: activeTab === 'action' }" @click="activeTab = 'action'" href="#" role="tab">
                  <i class="fas fa-edit"></i> Thực Hiện Chốt Sổ
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'" href="#" role="tab">
                  <i class="fas fa-history"></i> Xem Lại Lịch Sử
                </a>
              </li>
            </ul>
          </div>

          <div class="card-body">
            <div class="tab-content">

              <div class="tab-pane fade" :class="{ 'show active': activeTab === 'action' }">
                <div class="alert alert-warning">
                  <i class="fas fa-exclamation-triangle"></i> <b>Lưu ý:</b> Thao tác này sẽ ghi đè dữ liệu tồn đầu/tồn cuối của năm được chọn.
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
                      <select class="form-control" v-model="filters.warehouseId" :disabled="!isAdmin">
                        <option v-for="kho in khoList" :key="kho.maKho" :value="kho.maKho">{{ kho.tenKho }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-lg-2 col-md-12 col-12 d-flex align-items-end">
                    <div class="form-group w-100">
                      <button type="button" class="btn btn-warning btn-block" @click="thucHienChotSo" :disabled="loading">
                        <i class="fas" :class="loading ? 'fa-spinner fa-spin' : 'fa-save'"></i>
                        {{ loading ? 'Đang xử lý...' : 'Chốt Sổ Kho Này' }}
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
                    <div>
                      <button type="button" class="btn btn-sm btn-info mr-2" @click="printToWord(filters.nam, filters.warehouseId, currentTenKho, 'action')" :disabled="isExporting">
                        <i class="fas" :class="isExporting ? 'fa-spinner fa-spin' : 'fa-file-word'"></i>
                        {{ isExporting ? 'Đang xuất...' : 'In File Word' }}
                      </button>
                      <button type="button" class="btn btn-sm btn-success" @click="exportToExcel(filters.nam, filters.warehouseId, currentTenKho, 'action')" :disabled="isExportingExcel">
                        <i class="fas" :class="isExportingExcel ? 'fa-spinner fa-spin' : 'fa-file-excel'"></i>
                        {{ isExportingExcel ? 'Đang xuất...' : 'Xuất Excel' }}
                      </button>
                    </div>
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
                          <td class="text-center">{{ (actionPagination.page * actionPagination.size) + index + 1 }}</td>
                          <td class="text-primary font-weight-bold">{{ item.maSP }}</td>
                          <td>{{ formatProductName(item.tenSP) }}</td>
                          <td class="text-center">{{ item.donvitinh }}</td>
                          <td class="text-right">{{ item.tonDau }}</td>
                          <td class="text-right text-success">{{ item.nhapTrong }}</td>
                          <td class="text-right text-danger">{{ item.xuatTrong }}</td>
                          <td class="text-right font-weight-bold bg-warning bg-opacity-25">{{ item.tonCuoi }}</td>
                          <td class="text-right font-weight-bold text-primary">{{ formatCurrency(item.thanhTien) }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div class="bg-light border-top py-2" v-if="reportData.length > 0" style="border-bottom-left-radius: .25rem; border-bottom-right-radius: .25rem; box-shadow: 0 -1px 3px rgba(0,0,0,0.05);">
                    <div class="d-flex justify-content-around text-center align-items-center" style="font-size: 14px; min-height: 40px;">
                      <div class="flex-fill border-right">
                        <span class="text-muted mr-1">Tồn Đầu:</span>
                        <span class="font-weight-bold">{{ grandTotalAction.tdk }}</span>
                      </div>
                      <div class="flex-fill border-right">
                        <span class="text-success mr-1">Nhập:</span>
                        <span class="font-weight-bold">{{ grandTotalAction.ntk }}</span>
                      </div>
                      <div class="flex-fill border-right">
                        <span class="text-danger mr-1">Xuất:</span>
                        <span class="font-weight-bold">{{ grandTotalAction.xtk }}</span>
                      </div>
                      <div class="flex-fill border-right">
                        <span class="text-dark mr-1">Tồn Cuối:</span>
                        <span class="font-weight-bold text-warning" style="font-size: 1.1em;">{{ grandTotalAction.tck }}</span>
                      </div>
                      <div class="flex-fill">
                        <span class="text-primary mr-1">Tổng Tiền:</span>
                        <span class="font-weight-bold text-primary">{{ formatCurrency(grandTotalAction.tien) }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="d-flex justify-content-center mt-3 px-3 pb-3" v-if="actionPagination.totalPages > 1">
                    <ul class="pagination pagination-sm m-0">
                      <li class="page-item" :class="{ disabled: actionPagination.page === 0 }">
                        <a class="page-link" href="#" @click.prevent="changeActionPage(actionPagination.page - 1)">« Trước</a>
                      </li>
                      <li v-for="(page, index) in visibleActionPages" :key="index" class="page-item" :class="{ active: page === actionPagination.page + 1, disabled: page === '...' }">
                        <a class="page-link" href="#" @click.prevent="page !== '...' ? changeActionPage(page - 1) : null">{{ page }}</a>
                      </li>
                      <li class="page-item" :class="{ disabled: actionPagination.page >= actionPagination.totalPages - 1 }">
                        <a class="page-link" href="#" @click.prevent="changeActionPage(actionPagination.page + 1)">Sau »</a>
                      </li>
                    </ul>
                  </div>

                </div>
              </div>

              <div class="tab-pane fade" :class="{ 'show active': activeTab === 'history' }">
                <div class="row">
                  <div class="col-lg-3 col-md-6 col-12">
                    <div class="form-group">
                      <label>Xem Năm</label>
                      <input type="number" class="form-control" v-model="historyFilters.nam" placeholder="Nhập năm...">
                    </div>
                  </div>
                  <div class="col-lg-3 col-md-6 col-12">
                    <div class="form-group">
                      <label>Chọn Kho</label>
                      <select class="form-control" v-model="historyFilters.warehouseId" :disabled="!isAdmin">
                        <option :value="0" v-if="isAdmin">Tất cả kho</option>
                        <option v-for="kho in khoList" :key="kho.maKho" :value="kho.maKho">{{ kho.tenKho }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-lg-3 col-md-6 col-12">
                    <div class="form-group">
                      <label>Trạng thái máy (Lọc nhanh)</label>
                      <select class="form-control" v-model="historyFilters.statusId" @change="applyHistoryFilter">
                        <option v-for="st in statusList" :key="st.id" :value="st.id">{{ st.name }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="col-lg-3 col-md-12 col-12 d-flex align-items-end">
                    <div class="form-group w-100">
                      <button type="button" class="btn btn-primary btn-block" @click="fetchHistoryData" :disabled="loadingHistory">
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
                    <div>
                      <button type="button" class="btn btn-sm btn-info mr-2" @click="printToWord(historyFilters.nam, historyFilters.warehouseId, historyTenKho, 'history')" :disabled="isExporting">
                        <i class="fas" :class="isExporting ? 'fa-spinner fa-spin' : 'fa-file-word'"></i>
                        {{ isExporting ? 'Đang xuất...' : 'In File Word' }}
                      </button>
                      <button type="button" class="btn btn-sm btn-success" @click="exportToExcel(historyFilters.nam, historyFilters.warehouseId, historyTenKho, 'history')" :disabled="isExportingExcel">
                        <i class="fas" :class="isExportingExcel ? 'fa-spinner fa-spin' : 'fa-file-excel'"></i>
                        {{ isExportingExcel ? 'Đang xuất...' : 'Xuất Excel' }}
                      </button>
                    </div>
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
                          <td class="text-center">{{ (historyPagination.page * historyPagination.size) + index + 1 }}</td>
                          <td class="text-primary font-weight-bold">{{ item.maSP }}</td>
                          <td>{{ formatProductName(item.tenSP) }}</td>
                          <td class="text-center">{{ item.donvitinh }}</td>
                          <td class="text-right">{{ item.tonDau }}</td>
                          <td class="text-right text-success">{{ item.nhapTrong }}</td>
                          <td class="text-right text-danger">{{ item.xuatTrong }}</td>
                          <td class="text-right font-weight-bold bg-warning bg-opacity-25">{{ item.tonCuoi }}</td>
                          <td class="text-right font-weight-bold text-primary">{{ formatCurrency(item.thanhTien) }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div class="bg-light border-top py-2" v-if="historyData.length > 0" style="border-bottom-left-radius: .25rem; border-bottom-right-radius: .25rem; box-shadow: 0 -1px 3px rgba(0,0,0,0.05);">
                    <div class="d-flex justify-content-around text-center align-items-center" style="font-size: 14px; min-height: 40px;">
                      <div class="flex-fill border-right">
                        <span class="text-muted mr-1">Tồn Đầu:</span>
                        <span class="font-weight-bold">{{ grandTotalHistory.tdk }}</span>
                      </div>
                      <div class="flex-fill border-right">
                        <span class="text-success mr-1">Nhập:</span>
                        <span class="font-weight-bold">{{ grandTotalHistory.ntk }}</span>
                      </div>
                      <div class="flex-fill border-right">
                        <span class="text-danger mr-1">Xuất:</span>
                        <span class="font-weight-bold">{{ grandTotalHistory.xtk }}</span>
                      </div>
                      <div class="flex-fill border-right">
                        <span class="text-dark mr-1">Tồn Cuối:</span>
                        <span class="font-weight-bold text-warning" style="font-size: 1.1em;">{{ grandTotalHistory.tck }}</span>
                      </div>
                      <div class="flex-fill">
                        <span class="text-primary mr-1">Tổng Tiền:</span>
                        <span class="font-weight-bold text-primary">{{ formatCurrency(grandTotalHistory.tien) }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="d-flex justify-content-center mt-3 px-3 pb-3" v-if="historyPagination.totalPages > 1">
                    <ul class="pagination pagination-sm m-0">
                      <li class="page-item" :class="{ disabled: historyPagination.page === 0 }">
                        <a class="page-link" href="#" @click.prevent="changeHistoryPage(historyPagination.page - 1)">« Trước</a>
                      </li>
                      <li v-for="(page, index) in visibleHistoryPages" :key="index" class="page-item" :class="{ active: page === historyPagination.page + 1, disabled: page === '...' }">
                        <a class="page-link" href="#" @click.prevent="page !== '...' ? changeHistoryPage(page - 1) : null">{{ page }}</a>
                      </li>
                      <li class="page-item" :class="{ disabled: historyPagination.page >= historyPagination.totalPages - 1 }">
                        <a class="page-link" href="#" @click.prevent="changeHistoryPage(historyPagination.page + 1)">Sau »</a>
                      </li>
                    </ul>
                  </div>

                </div>
                <div v-else-if="searchedHistory" class="text-center mt-5 text-muted">
                  <p><i class="fas fa-search mb-2" style="font-size: 2rem;"></i><br>Không tìm thấy dữ liệu chốt sổ cho năm/kho/trạng thái này.</p>
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
import ExcelJS from 'exceljs'; 

const API_BASE = '/thong-ke';

const activeTab = ref('action');
const khoList = ref([]);
const isExporting = ref(false);
const isExportingExcel = ref(false);
const isAdmin = ref(false);

const isChotSoState = ref(true);
const showFirstYearWarning = () => {
  alert("LƯU Ý:\nĐây là năm đầu tiên sử dụng phần mềm, hệ thống chưa có dữ liệu chốt sổ của năm trước.\n\nDo đó, Tồn Đầu Kỳ sẽ được mặc định là 0.");
};

const formatProductName = (name) => {
  if (!name) return '';
  let formattedName = name.toString();
  formattedName = formattedName.replace(/\s*-\s*Bình thường\s*$/gi, '');
  formattedName = formattedName.replace(/\s*-\s*Chưa xác định\s*$/gi, '');
  formattedName = formattedName.replace(/\s*-\s*Mới\s*\(New\)/gi, ' - New');
  formattedName = formattedName.replace(/\s*-\s*Mới/gi, ' - New');
  formattedName = formattedName.replace(/\s*-\s*$/g, '');
  return formattedName.trim();
};

const statusList = ref([
  { id: 0, name: 'Tất cả' },
  { id: 1, name: 'Bình thường' },
  { id: 2, name: 'New' },
  { id: 3, name: 'Like New' },
  { id: 4, name: 'Hỏng' },
  { id: 5, name: 'Xác' },
  { id: 6, name: 'Thu hồi' },
  { id: 7, name: 'Nhập Khẩu' },
]);

// Lưu trữ Dữ liệu gốc lấy từ Backend
const allActionData = ref([]); 
const allHistoryData = ref([]); 
const filteredHistoryData = ref([]); 

// Lưu trữ dữ liệu đang hiển thị trên 1 trang
const reportData = ref([]);
const historyData = ref([]);

const grandTotalAction = ref({ tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });
const grandTotalHistory = ref({ tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });

const filters = reactive({ nam: new Date().getFullYear() - 1, warehouseId: '' });
const loading = ref(false);
const currentTenKho = ref('');
const actionPagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });

const historyFilters = reactive({ nam: new Date().getFullYear(), warehouseId: 0, statusId: 0 });
const loadingHistory = ref(false);
const historyTenKho = ref('');
const searchedHistory = ref(false);
const historyPagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });

const setupPhanQuyen = () => {
  const role = localStorage.getItem('userRole');
  let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
  if (!userMaKho) {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      userMaKho = userInfo.maKho;
    } catch (e) {}
  }
  if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
    isAdmin.value = true;
  } else {
    isAdmin.value = false;
    const myKho = userMaKho ? parseInt(userMaKho) : 0;
    filters.warehouseId = myKho;
    historyFilters.warehouseId = myKho;
  }
};

const visibleActionPages = computed(() => {
  const total = actionPagination.totalPages;
  if (total === 0) return [];
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

const visibleHistoryPages = computed(() => {
  const total = historyPagination.totalPages;
  if (total === 0) return [];
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

// ==========================================
// LOGIC TAB 1: THỰC HIỆN CHỐT SỔ (PHÂN TRANG FRONTEND)
// ==========================================
const thucHienChotSo = async () => {
  if (!filters.warehouseId) {
      alert("Vui lòng chọn kho để chốt sổ!");
      return;
  }
  if (!confirm(`Bạn có chắc chắn muốn chốt sổ cho kho này trong năm ${filters.nam}? Dữ liệu cũ sẽ bị đè!`)) return;
  actionPagination.page = 0;
  goiApiChotSo();
};

const goiApiChotSo = async () => {
  loading.value = true;
  try {
    await api.post(`${API_BASE}/chot-so`, null, {
      params: { nam: filters.nam, maKho: filters.warehouseId }
    });
    alert("Chốt sổ thành công!");
    await goiApiXemKetQuaSauChot();
  } catch (error) {
    const msg = error.response?.data?.message || error.message;
    if (error.response && error.response.data && typeof error.response.data === 'string') {
        alert("Lỗi khi chốt sổ: " + error.response.data);
    } else {
        alert("Lỗi khi chốt sổ: " + msg);
    }
  } finally {
    loading.value = false;
  }
};

const goiApiXemKetQuaSauChot = async () => {
  loading.value = true;
  try {
    const namKetQua = parseInt(filters.nam) + 1;
    
    // Yêu cầu Backend trả toàn bộ dữ liệu (size lớn) 1 lần
    const response = await api.get(`${API_BASE}/lich-su`, {
      params: { nam: namKetQua, maKho: filters.warehouseId, page: 0, size: 999999 }
    });
    
    currentTenKho.value = response.data.tenKho;
    allActionData.value = response.data.danhSachChiTiet || [];
    
    // Tính tổng trên TOÀN BỘ dữ liệu
    grandTotalAction.value = allActionData.value.reduce((acc, item) => {
      acc.tdk += Number(item.tonDau || 0);
      acc.ntk += Number(item.nhapTrong || 0);
      acc.xtk += Number(item.xuatTrong || 0);
      acc.tck += Number(item.tonCuoi || 0);
      acc.tien += Number(item.thanhTien || 0);
      return acc;
    }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });

    actionPagination.total = allActionData.value.length;
    actionPagination.totalPages = Math.ceil(actionPagination.total / actionPagination.size);
    actionPagination.page = 0;

    applyActionPagination();

  } catch (e) {
    console.error("Lỗi xem kết quả chốt:", e);
  } finally {
    loading.value = false;
  }
};

const applyActionPagination = () => {
  const start = actionPagination.page * actionPagination.size;
  reportData.value = allActionData.value.slice(start, start + actionPagination.size);
};

const changeActionPage = (newPage) => {
  if (newPage < 0 || newPage >= actionPagination.totalPages) return;
  actionPagination.page = newPage;
  applyActionPagination();
};

// ==========================================
// LOGIC TAB 2: XEM LỊCH SỬ (FRONTEND FILTER & PAGINATION)
// ==========================================
const fetchHistoryData = async () => {
  loadingHistory.value = true;
  searchedHistory.value = true;
  try {
    // Lấy toàn bộ dữ liệu lịch sử lưu vào RAM máy khách (size 999999)
    const response = await api.get(`${API_BASE}/lich-su`, {
      params: { nam: historyFilters.nam, maKho: historyFilters.warehouseId, page: 0, size: 999999 }
    });
    historyTenKho.value = response.data.tenKho;
    allHistoryData.value = response.data.danhSachChiTiet || [];
    
    // Kích hoạt bộ lọc trên Frontend
    applyHistoryFilter();
    
  } catch (error) {
    alert("Lỗi tải dữ liệu lịch sử!");
  } finally {
    loadingHistory.value = false;
  }
};

// Hàm lọc trạng thái chỉ chạy trên dữ liệu đã tải trong RAM
const applyHistoryFilter = () => {
  let filtered = allHistoryData.value;
  const statusId = historyFilters.statusId;

  if (statusId !== 0) {
    const st = statusList.value.find(s => s.id === statusId);
    if (st) {
      if (st.id === 1) { 
        // Lọc máy "Bình thường" (Không chứa các đuôi trạng thái khác)
        filtered = filtered.filter(item => {
          return !statusList.value.some(s => s.id > 1 && item.tenSP.endsWith(' - ' + s.name));
        });
      } else {
        // Lọc theo đuôi trạng thái
        filtered = filtered.filter(item => item.tenSP.endsWith(' - ' + st.name));
      }
    }
  }

  filteredHistoryData.value = filtered;

  // Tính Tổng lại Dựa trên Danh sách đã Lọc
  grandTotalHistory.value = filtered.reduce((acc, item) => {
    acc.tdk += Number(item.tonDau || 0);
    acc.ntk += Number(item.nhapTrong || 0);
    acc.xtk += Number(item.xuatTrong || 0);
    acc.tck += Number(item.tonCuoi || 0);
    acc.tien += Number(item.thanhTien || 0);
    return acc;
  }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });

  historyPagination.total = filtered.length;
  historyPagination.totalPages = Math.ceil(filtered.length / historyPagination.size);
  historyPagination.page = 0;

  applyHistoryPagination();
};

const applyHistoryPagination = () => {
  const start = historyPagination.page * historyPagination.size;
  historyData.value = filteredHistoryData.value.slice(start, start + historyPagination.size);
};

const changeHistoryPage = (newPage) => {
  if (newPage < 0 || newPage >= historyPagination.totalPages) return;
  historyPagination.page = newPage;
  applyHistoryPagination();
};

// ==========================================
// XUẤT FILE WORD (Dùng Dữ Liệu Tự Tính Trong RAM)
// ==========================================
const loadFile = async (url) => {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`Không thể tải file mẫu: ${url}`);
  return await response.arrayBuffer();
};

const printToWord = async (namInput, maKhoInput, tenKhoString, tabName) => {
  if (isExporting.value) return;
  isExporting.value = true;

  try {
    let namCanLay = tabName === 'action' ? parseInt(namInput) + 1 : namInput;
    
    // LẤY DỮ LIỆU ĐÃ LỌC TRÊN FRONTEND (Không gọi API lại)
    const sourceData = tabName === 'action' ? allActionData.value : filteredHistoryData.value;
    
    if (sourceData.length === 0) {
      alert("Không có dữ liệu chốt sổ để in.");
      isExporting.value = false;
      return;
    }

    const tenKhoChinhXac = tenKhoString || "Kho hệ thống";
    const content = await loadFile("/File_Mau_BaoCaoChotSoNam.docx");
    const zip = new PizZip(content);
    const doc = new Docxtemplater(zip, { paragraphLoop: true, linebreaks: true });

    // Lấy Tổng cộng có sẵn
    const totals = tabName === 'action' ? grandTotalAction.value : grandTotalHistory.value;

    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    let hours = today.getHours();
    const minutes = String(today.getMinutes()).padStart(2, '0');
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12; 
    hours = hours ? hours : 12; 

    const dataToRender = {
      nam: namCanLay,
      tenKho: tenKhoChinhXac,
      d: dd, m: mm, y: yyyy, h: String(hours).padStart(2, '0'), ph: minutes, ampm: ampm,
      sumTDK: formatCurrency(totals.tdk),
      sumNTK: formatCurrency(totals.ntk),
      sumXTK: formatCurrency(totals.xtk),
      sumTCK: formatCurrency(totals.tck),
      sumTien: formatCurrency(totals.tien),
      p: sourceData.map((item, index) => ({
        stt: index + 1,
        ma: item.maSP || "",
        ten: formatProductName(item.tenSP) || "",
        dvt: item.donvitinh || "",
        tdk: formatCurrency(item.tonDau),
        ntk: formatCurrency(item.nhapTrong),
        xtk: formatCurrency(item.xuatTrong),
        tck: formatCurrency(item.tonCuoi),
        gia: formatCurrency(item.giaBQ || 0),
        tien: formatCurrency(item.thanhTien || 0)
      }))
    };

    doc.render(dataToRender);
    const out = doc.getZip().generate({
      type: "blob",
      mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    });

    saveAs(out, `BienBanChotSo_${tenKhoChinhXac.replace(/\s+/g, '_')}_Nam_${namCanLay}.docx`);

  } catch (error) {
    console.error("Lỗi in Word:", error);
    alert("Có lỗi xảy ra khi xuất file Word: " + error.message);
  } finally {
    isExporting.value = false;
  }
};

// ==========================================
// XUẤT FILE EXCEL (Dùng Dữ Liệu Tự Tính Trong RAM)
// ==========================================
const exportToExcel = async (namInput, maKhoInput, tenKhoString, tabName) => {
  if (isExportingExcel.value) return;
  isExportingExcel.value = true;

  try {
    let namCanLay = tabName === 'action' ? parseInt(namInput) + 1 : namInput;
    
    // LẤY DỮ LIỆU ĐÃ LỌC TRÊN FRONTEND (Không gọi API lại)
    const sourceData = tabName === 'action' ? allActionData.value : filteredHistoryData.value;
    
    if (sourceData.length === 0) {
      alert("Không có dữ liệu chốt sổ để xuất Excel.");
      isExportingExcel.value = false;
      return;
    }

    const tenKhoChinhXac = tenKhoString || "Kho hệ thống";
    const workbook = new ExcelJS.Workbook();
    const sheet = workbook.addWorksheet('Biên Bản Chốt Sổ');

    sheet.getCell('A1').value = 'BIÊN BẢN CHỐT TỒN KHO';
    sheet.getCell('A1').font = { size: 16, bold: true };
    sheet.mergeCells('A1:J1');

    sheet.getCell('A2').value = `Năm chốt sổ:`;
    sheet.getCell('C2').value = namCanLay;
    
    sheet.getCell('A3').value = `Kho:`;
    sheet.getCell('C3').value = tenKhoChinhXac;
    sheet.getCell('C3').font = { bold: true };

    const headerRow = sheet.addRow([
      'STT', 'Mã sản phẩm', 'Tên sản phẩm', 'ĐVT',
      'Tồn Đầu', 'Nhập', 'Xuất', 'Tồn Cuối', 'Giá/BQ', 'Thành tiền'
    ]);
    headerRow.font = { bold: true };
    headerRow.alignment = { vertical: 'middle', horizontal: 'center' };

    sheet.columns = [
      { key: 'stt', width: 5 },
      { key: 'maSP', width: 20 },
      { key: 'tenSP', width: 45 },
      { key: 'dvt', width: 8 },
      { key: 'tdk', width: 10 },
      { key: 'ntk', width: 10 },
      { key: 'xtk', width: 10 },
      { key: 'tck', width: 10 },
      { key: 'gia', width: 15 },
      { key: 'tien', width: 18 }
    ];

    sourceData.forEach((item, index) => {
      const row = sheet.addRow([
        index + 1,
        item.maSP,
        formatProductName(item.tenSP), 
        item.donvitinh,
        item.tonDau || 0,
        item.nhapTrong || 0,
        item.xuatTrong || 0,
        item.tonCuoi || 0,
        item.giaBQ || 0,
        item.thanhTien || 0
      ]);

      for (let i = 5; i <= 10; i++) {
        row.getCell(i).alignment = { horizontal: 'right' };
      }
    });

    const totals = tabName === 'action' ? grandTotalAction.value : grandTotalHistory.value;
    const totalRow = sheet.addRow(['Tổng Cộng', '', '', '', totals.tdk, totals.ntk, totals.xtk, totals.tck, '', totals.tien]);
    totalRow.font = { bold: true };
    sheet.mergeCells(`A${totalRow.number}:D${totalRow.number}`);
    totalRow.getCell(1).alignment = { horizontal: 'center' };
    
    for (let i = 5; i <= 10; i++) {
      totalRow.getCell(i).alignment = { horizontal: 'right' };
    }

    sheet.eachRow((row, rowNumber) => {
      if (rowNumber >= 4 && rowNumber <= totalRow.number) {
        row.eachCell((cell) => {
          cell.border = {
            top: { style: 'thin' }, left: { style: 'thin' },
            bottom: { style: 'thin' }, right: { style: 'thin' }
          };
        });
      }
    });

    const fileName = `BienBanChotSo_${tenKhoChinhXac.replace(/\s+/g, '_')}_Nam_${namCanLay}.xlsx`;
    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    saveAs(blob, fileName);

  } catch (error) {
    console.error("Lỗi xuất Excel:", error);
    alert("Có lỗi xảy ra khi xuất file Excel.");
  } finally {
    isExportingExcel.value = false;
  }
};


const loadKho = async () => {
  try {
    const res = await api.get('/kho');
    khoList.value = res.data;
    
    if (res.data.length > 0) {
        filters.warehouseId = res.data[0].maKho;
        historyFilters.warehouseId = isAdmin.value ? 0 : res.data[0].maKho;
    }
  } catch (e) {
    console.error("Lỗi tải kho:", e);
  }
};

onMounted(async () => {
  setupPhanQuyen();
  await loadKho();
});
</script>

<style scoped>
.card-primary.card-outline-tabs>.card-header a.active {
  border-top: 3px solid #ffc107;
}

.nav-link,
.page-link {
  cursor: pointer;
  font-weight: 600;
}

.page-item.active .page-link {
  background-color: #007bff;
  border-color: #007bff;
  color: white;
}

.page-item.disabled .page-link {
  pointer-events: none;
  background-color: #fff;
  color: #6c757d;
}

.form-control-sm {
  height: calc(1.8125rem + 2px);
  font-size: .875rem;
}

.table-container {
  max-height: 65vh;
  overflow: auto;
  position: relative;
  border-top: 1px solid #dee2e6;
  scrollbar-width: thin;
}

.table-container::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.table-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.sticky-thead th {
  position: sticky;
  top: 0;
  background-color: #f8f9fa;
  z-index: 10;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  white-space: nowrap;
}

@media (max-width: 768px) {
  .nav-tabs .nav-link {
    padding: 0.5rem 0.8rem;
    font-size: 0.9rem;
  }

  .col-6 {
    padding-left: 5px;
    padding-right: 5px;
  }

  .table th,
  .table td {
    padding: 8px 6px;
    font-size: 13px;
  }
}
</style>