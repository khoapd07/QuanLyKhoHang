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
                      <label>Năm Cần Chốt Tồn Đầu</label>
                      <input type="number" class="form-control" v-model="filters.nam"
                        placeholder="--- Chọn năm cần chốt sổ ---">
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
                      <button type="button" class="btn btn-warning btn-block" @click="thucHienChotSo"
                        :disabled="loading">
                        <i class="fas" :class="loading ? 'fa-spinner fa-spin' : 'fa-save'"></i>
                        {{ loading ? 'Đang xử lý...' : 'Chốt Sổ Kho Này' }}
                      </button>
                    </div>
                  </div>
                </div>

                <div class="mt-4">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <h5 class="text-success m-0" v-if="reportData.length > 0">
                      <i class="fas fa-check-circle"></i> Kết quả: {{ currentTenKho }}
                      <span class="badge badge-secondary ml-2">Tổng: {{ actionPagination.total }} dòng</span>
                    </h5>
                    <h5 v-else></h5>

                    <div>
                      <button type="button" class="btn btn-sm btn-dark mr-2"
                        @click="printReport(filters.nam, currentTenKho, 'action')"
                        :disabled="loading || allActionData.length === 0">
                        <i class="fas fa-print"></i> In Báo Cáo
                      </button>
                      <button type="button" class="btn btn-sm btn-info mr-2"
                        @click="printToWord(filters.nam, filters.warehouseId, currentTenKho, 'action')"
                        :disabled="isExporting || allActionData.length === 0">
                        <i class="fas" :class="isExporting ? 'fa-spinner fa-spin' : 'fa-file-word'"></i> {{ isExporting
                          ? 'Đang xuất...' : 'In File Word' }}
                      </button>
                      <button type="button" class="btn btn-sm btn-success"
                        @click="exportToExcel(filters.nam, filters.warehouseId, currentTenKho, 'action')"
                        :disabled="isExportingExcel || allActionData.length === 0">
                        <i class="fas" :class="isExportingExcel ? 'fa-spinner fa-spin' : 'fa-file-excel'"></i> {{
                          isExportingExcel ? 'Đang xuất...' : 'Xuất Excel' }}
                      </button>

                      <router-link to="/nhap-excel" class="btn btn-sm btn-success ml-3 shadow-sm font-weight-bold">
                        <i class="fas fa-file-excel mr-1"></i> Import Từ Excel
                      </router-link>
                      <router-link to="/xoa-du-lieu-ton-kho" class="btn btn-sm btn-danger ml-2 font-weight-bold shadow-sm">
                        <i class="fas fa-trash-alt mr-1"></i> Xóa Tồn đầu năm
                      </router-link>
                    </div>
                  </div>

                  <div class="table-responsive" style="max-height: 500px;">
                    <table class="table table-bordered table-striped table-head-fixed text-nowrap">
                      <thead>
                        <tr>
                          <th class="text-center" width="50">STT</th>
                          <th>Mã SP</th>
                          <th>Tên Sản Phẩm </th>
                          <th class="text-center">ĐVT</th>
                          <th class="text-right">Tồn Đầu</th>
                          <th class="text-right">Nhập</th>
                          <th class="text-right">Xuất</th>
                          <th class="text-right">Tồn Cuối</th>
                          <th class="text-right">Giá BQ</th>
                          <th class="text-right">Thành Tiền</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="reportData.length === 0">
                          <td colspan="10" class="text-center py-4 text-muted">Chưa có dữ liệu chốt sổ.</td>
                        </tr>
                        <tr v-for="(item, index) in reportData" :key="index">
                          <td class="text-center">{{ (actionPagination.page * actionPagination.size) + index + 1 }}</td>
                          <td class="text-primary font-weight-bold font-monospace">{{ item.maSP }}</td>
                          <td>{{ formatProductName(item.tenSP) }}</td>
                          <td class="text-center">{{ item.donvitinh }}</td>
                          <td class="text-right">{{ item.tonDau || 0 }}</td>
                          <td class="text-right text-success">+{{ item.nhapTrong || 0 }}</td>
                          <td class="text-right text-danger">-{{ item.xuatTrong || 0 }}</td>
                          <td class="text-right font-weight-bold bg-warning bg-opacity-25">{{ item.tonCuoi || 0 }}</td>
                          <td class="text-right">{{ formatCurrency(item.giaBQ || 0) }}</td>
                          <td class="text-right font-weight-bold text-primary">{{ formatCurrency(item.thanhTien || 0) }}
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div class="bg-light border-top py-2" v-if="reportData.length > 0"
                    style="border-bottom-left-radius: .25rem; border-bottom-right-radius: .25rem; box-shadow: 0 -1px 3px rgba(0,0,0,0.05);">
                    <div class="d-flex justify-content-around text-center align-items-center"
                      style="font-size: 14px; min-height: 40px;">
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
                        <span class="font-weight-bold text-warning" style="font-size: 1.1em;">{{ grandTotalAction.tck
                        }}</span>
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
                        <a class="page-link" href="#" @click.prevent="changeActionPage(actionPagination.page - 1)">«
                          Trước</a>
                      </li>
                      <li v-for="(page, index) in visibleActionPages" :key="index" class="page-item"
                        :class="{ active: page === actionPagination.page + 1, disabled: page === '...' }">
                        <a class="page-link" href="#"
                          @click.prevent="page !== '...' ? changeActionPage(page - 1) : null">{{ page }}</a>
                      </li>
                      <li class="page-item"
                        :class="{ disabled: actionPagination.page >= actionPagination.totalPages - 1 }">
                        <a class="page-link" href="#" @click.prevent="changeActionPage(actionPagination.page + 1)">Sau
                          »</a>
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
                      <select class="form-control" v-model="historyFilters.warehouseId" :disabled="!isAdmin"
                        @change="fetchHistoryData">
                        <option v-for="kho in khoList" :key="kho.maKho" :value="kho.maKho">{{ kho.tenKho }}</option>
                      </select>
                    </div>
                  </div>

                  <div class="col-lg-3 col-md-6 col-12">
                    <div class="form-group custom-multi-select" ref="statusDropdownRef">
                      <label>Trạng thái máy</label>
                      <div class="form-control dropdown-trigger" :class="{ 'is-focused': isStatusDropdownOpen }"
                        @click="toggleStatusDropdown">
                        <span class="dropdown-text">{{ selectedStatusText }}</span>
                        <i class="fas fa-chevron-down dropdown-icon"></i>
                      </div>

                      <div class="dropdown-menu-custom shadow-sm" v-show="isStatusDropdownOpen">
                        <div class="dropdown-list">
                          <label class="dropdown-item-custom" v-for="st in statusList" :key="st.id">
                            <input type="checkbox" class="checkbox-ui" :value="st.id" v-model="historyFilters.statusIds"
                              @change="handleStatusChange(st.id)">
                            <span class="ml-2">{{ st.name }}</span>
                          </label>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="col-lg-3 col-md-12 col-12 d-flex align-items-end">
                    <div class="form-group w-100">
                      <button type="button" class="btn btn-primary btn-block" @click="fetchHistoryData"
                        :disabled="loadingHistory">
                        <i class="fas" :class="loadingHistory ? 'fa-spinner fa-spin' : 'fa-search'"></i>
                        {{ loadingHistory ? 'Đang tải...' : 'Xem Dữ Liệu' }}
                      </button>
                    </div>
                  </div>
                </div>

                <div class="mt-4">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <h5 class="text-primary m-0" v-if="historyData.length > 0">
                      <i class="fas fa-list-alt"></i> Dữ liệu lưu trữ: {{ historyTenKho }}
                      <span class="badge badge-secondary ml-2">Tổng: {{ historyPagination.total }} dòng</span>
                    </h5>
                    <h5 v-else-if="searchedHistory" class="text-muted m-0"><i class="fas fa-search"></i> Không tìm thấy
                      dữ liệu</h5>
                    <h5 v-else></h5>

                    <div>
                      <button type="button" class="btn btn-sm btn-dark mr-2"
                        @click="printReport(historyFilters.nam, historyTenKho, 'history')"
                        :disabled="loadingHistory || filteredHistoryData.length === 0">
                        <i class="fas fa-print"></i> In Báo Cáo
                      </button>
                      <button type="button" class="btn btn-sm btn-info mr-2"
                        @click="printToWord(historyFilters.nam, historyFilters.warehouseId, historyTenKho, 'history')"
                        :disabled="isExporting || filteredHistoryData.length === 0">
                        <i class="fas" :class="isExporting ? 'fa-spinner fa-spin' : 'fa-file-word'"></i> {{ isExporting
                          ? 'Đang xuất...' : 'In File Word' }}
                      </button>
                      <button type="button" class="btn btn-sm btn-success"
                        @click="exportToExcel(historyFilters.nam, historyFilters.warehouseId, historyTenKho, 'history')"
                        :disabled="isExportingExcel || filteredHistoryData.length === 0">
                        <i class="fas" :class="isExportingExcel ? 'fa-spinner fa-spin' : 'fa-file-excel'"></i> {{
                          isExportingExcel ? 'Đang xuất...' : 'Xuất Excel' }}
                      </button>
                    </div>
                  </div>

                  <div class="table-responsive" style="max-height: 500px;">
                    <table class="table table-bordered table-striped table-head-fixed text-nowrap">
                      <thead>
                        <tr>
                          <th class="text-center" width="50">STT</th>
                          <th>Mã SP</th>
                          <th>Tên Sản Phẩm </th>
                          <th class="text-center">ĐVT</th>
                          <th class="text-right">Tồn Đầu</th>
                          <th class="text-right">Nhập</th>
                          <th class="text-right">Xuất</th>
                          <th class="text-right">Tồn Cuối</th>
                          <th class="text-right">Giá BQ</th>
                          <th class="text-right">Thành Tiền</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-if="historyData.length === 0 && !loadingHistory">
                          <td colspan="10" class="text-center py-4 text-muted">Không có dữ liệu.</td>
                        </tr>
                        <tr v-for="(item, index) in historyData" :key="index">
                          <td class="text-center">{{ (historyPagination.page * historyPagination.size) + index + 1 }}
                          </td>
                          <td class="text-primary font-weight-bold font-monospace">{{ item.maSP }}</td>
                          <td>{{ formatProductName(item.tenSP) }}</td>
                          <td class="text-center">{{ item.donvitinh }}</td>
                          <td class="text-right">{{ item.tonDau || 0 }}</td>
                          <td class="text-right text-success">+{{ item.nhapTrong || 0 }}</td>
                          <td class="text-right text-danger">-{{ item.xuatTrong || 0 }}</td>
                          <td class="text-right font-weight-bold bg-warning bg-opacity-25">{{ item.tonCuoi || 0 }}</td>
                          <td class="text-right">{{ formatCurrency(item.giaBQ || 0) }}</td>
                          <td class="text-right font-weight-bold text-primary">{{ formatCurrency(item.thanhTien || 0) }}
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <div class="bg-light border-top py-2" v-if="historyData.length > 0"
                    style="border-bottom-left-radius: .25rem; border-bottom-right-radius: .25rem; box-shadow: 0 -1px 3px rgba(0,0,0,0.05);">
                    <div class="d-flex justify-content-around text-center align-items-center"
                      style="font-size: 14px; min-height: 40px;">
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
                        <span class="font-weight-bold text-warning" style="font-size: 1.1em;">{{ grandTotalHistory.tck
                        }}</span>
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
                        <a class="page-link" href="#" @click.prevent="changeHistoryPage(historyPagination.page - 1)">«
                          Trước</a>
                      </li>
                      <li v-for="(page, index) in visibleHistoryPages" :key="index" class="page-item"
                        :class="{ active: page === historyPagination.page + 1, disabled: page === '...' }">
                        <a class="page-link" href="#"
                          @click.prevent="page !== '...' ? changeHistoryPage(page - 1) : null">{{ page }}</a>
                      </li>
                      <li class="page-item"
                        :class="{ disabled: historyPagination.page >= historyPagination.totalPages - 1 }">
                        <a class="page-link" href="#" @click.prevent="changeHistoryPage(historyPagination.page + 1)">Sau
                          »</a>
                      </li>
                    </ul>
                  </div>

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
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue';
import api from '@/utils/axios';
import { saveAs } from "file-saver";
import PizZip from "pizzip";
import Docxtemplater from "docxtemplater";
import ExcelJS from 'exceljs';
import Swal from 'sweetalert2';

const API_BASE = '/thong-ke';

// 1. QUẢN LÝ TRẠNG THÁI (STATE CHUNG)
const activeTab = ref('action');
const isExporting = ref(false);
const isExportingExcel = ref(false);
const isAdmin = ref(false);
const isChotSoState = ref(true);

const khoList = ref([]);
const statusList = ref([{ id: 0, name: 'Tất cả' }]);

const isStatusDropdownOpen = ref(false);
const statusDropdownRef = ref(null);

const loading = ref(false);
const currentTenKho = ref('');
const filters = reactive({ nam: new Date().getFullYear(), warehouseId: '' });
const allActionData = ref([]);
const reportData = ref([]);
const grandTotalAction = ref({ tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });
const actionPagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });

const loadingHistory = ref(false);
const searchedHistory = ref(false);
const historyTenKho = ref('');
const historyFilters = reactive({ nam: new Date().getFullYear(), warehouseId: '', statusIds: [0] });
const allHistoryData = ref([]);
const filteredHistoryData = ref([]);
const historyData = ref([]);
const grandTotalHistory = ref({ tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });
const historyPagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });

// 2. DỮ LIỆU TÍNH TOÁN TỰ ĐỘNG (COMPUTED)
const selectedStatusText = computed(() => {
  if (historyFilters.statusIds.includes(0)) return 'Tất cả trạng thái';
  return statusList.value.filter(s => historyFilters.statusIds.includes(s.id)).map(s => s.name).join(', ');
});

const generatePagination = (paginationInfo) => {
  const total = paginationInfo.totalPages;
  if (total === 0) return [];
  const current = paginationInfo.page + 1;
  const range = [], rangeWithDots = [];
  let l;
  for (let i = 1; i <= total; i++) {
    if (i === 1 || i === total || (i >= current - 2 && i <= current + 2)) range.push(i);
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
};

const visibleActionPages = computed(() => generatePagination(actionPagination));
const visibleHistoryPages = computed(() => generatePagination(historyPagination));

// 3. CÁC HÀM TIỆN ÍCH
const formatProductName = (name) => {
  if (!name) return '';
  return name.toString()
    .replace(/\s*-\s*Bình thường\s*$/gi, '')
    .replace(/\s*-\s*Chưa xác định\s*$/gi, '')
    .replace(/\s*-\s*Mới\s*\(New\)/gi, ' - New')
    .replace(/\s*-\s*Mới/gi, ' - New')
    .replace(/\s*-\s*$/g, '')
    .trim();
};

const formatCurrency = (value) => {
  if (value === null || value === undefined || isNaN(value)) return '0';
  return new Intl.NumberFormat('vi-VN', { style: 'decimal', currency: 'VND' }).format(value);
};

const showFirstYearWarning = () => {
  Swal.fire('Lưu ý hệ thống', 'Đây là năm đầu tiên sử dụng phần mềm, hệ thống chưa có dữ liệu chốt sổ của năm trước.\n\nDo đó, Tồn Đầu Kỳ sẽ được mặc định là 0.', 'info');
};

// 4. LOGIC TAB 1: THỰC HIỆN CHỐT SỔ
const thucHienChotSo = async () => {
  if (!filters.warehouseId) return Swal.fire('Cảnh báo', 'Vui lòng chọn kho để chốt sổ!', 'warning');

  const currentYear = new Date().getFullYear();
  if (parseInt(filters.nam) > currentYear) {
    return Swal.fire('Thao tác không hợp lệ', `Bạn chỉ có thể chốt số dư Tồn cho năm ${currentYear} trở về trước.`, 'error');
  }

  const { value: password } = await Swal.fire({
    title: 'Xác thực chốt sổ',
    html: `Bạn đang chuẩn bị chốt sổ kho cho năm <b>${filters.nam}</b>.<br><br><b>Lưu ý:</b> Dữ liệu cũ sẽ bị ghi đè. Vui lòng nhập mật khẩu tài khoản của bạn để xác nhận hành động này.`,
    input: 'password',
    inputPlaceholder: 'Nhập mật khẩu của bạn',
    inputAttributes: { autocapitalize: 'off', autocorrect: 'off' },
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#ffc107',
    cancelButtonColor: '#6c757d',
    confirmButtonText: 'Xác nhận chốt sổ',
    cancelButtonText: 'Hủy',
    inputValidator: (value) => {
      if (!value) return 'Mật khẩu không được để trống!'
    }
  });

  if (!password) return;

  actionPagination.page = 0;
  loading.value = true;

  try {
    let savedUsername = localStorage.getItem('hoTen') || localStorage.getItem('username') || localStorage.getItem('tenTaiKhoan') || '';
    try { const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}'); savedUsername = userInfo.hoTen || userInfo.tenTaiKhoan || userInfo.username || userInfo.sub || savedUsername; } catch(e) {}

    if (!savedUsername) {
      Swal.fire('Lỗi phiên đăng nhập', 'Không tìm thấy tên tài khoản của bạn. Vui lòng đăng xuất và đăng nhập lại!', 'error');
      loading.value = false;
      return;
    }

    const authRes = await api.post('/thong-ke/verify-password', { username: savedUsername, password: password });

    if (authRes.data.success) {
      await api.post(`${API_BASE}/chot-so`, null, { params: { nam: filters.nam, maKho: filters.warehouseId } });
      Swal.fire('Thành công!', `Đã chốt sổ thành công cho năm ${filters.nam}.`, 'success');
      await goiApiXemKetQuaSauChot();
    } else {
      Swal.fire('Lỗi xác thực', 'Mật khẩu không chính xác. Không thể thực hiện chốt sổ.', 'error');
    }
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || error.message;
    Swal.fire('Có lỗi xảy ra', `Lỗi khi chốt sổ: ${msg}`, 'error');
  } finally {
    loading.value = false;
  }
};

const goiApiXemKetQuaSauChot = async () => {
  try {
    const response = await api.get(`${API_BASE}/lich-su`, { params: { nam: parseInt(filters.nam), maKho: filters.warehouseId, page: 0, size: 999999 } });
    currentTenKho.value = response.data.tenKho;
    allActionData.value = response.data.danhSachChiTiet || [];
    grandTotalAction.value = calculateTotals(allActionData.value);
    actionPagination.total = allActionData.value.length;
    actionPagination.totalPages = Math.ceil(actionPagination.total / actionPagination.size);
    applyActionPagination();
  } catch (e) { console.error("Lỗi xem kết quả chốt:", e); }
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

// 5. LOGIC TAB 2: XEM LỊCH SỬ
const fetchHistoryData = async () => {
  if (!historyFilters.warehouseId) return;
  loadingHistory.value = true;
  searchedHistory.value = true;
  try {
    const response = await api.get(`${API_BASE}/lich-su`, { params: { nam: historyFilters.nam, maKho: historyFilters.warehouseId, page: 0, size: 999999 } });
    historyTenKho.value = response.data.tenKho;
    allHistoryData.value = response.data.danhSachChiTiet || [];
    applyHistoryFilter();
  } catch (error) { Swal.fire('Lỗi', 'Lỗi tải dữ liệu lịch sử!', 'error'); }
  finally { loadingHistory.value = false; }
};

const handleStatusChange = (changedId) => {
  if (changedId === 0 && historyFilters.statusIds.includes(0)) historyFilters.statusIds = [0];
  else if (changedId !== 0 && historyFilters.statusIds.includes(0)) historyFilters.statusIds = historyFilters.statusIds.filter(id => id !== 0);
  if (historyFilters.statusIds.length === 0) historyFilters.statusIds = [0];
  applyHistoryFilter();
};

const applyHistoryFilter = () => {
  let filtered = allHistoryData.value;
  const selectedIds = historyFilters.statusIds;

  // Nếu người dùng KHÔNG chọn "Tất cả" (Tất cả có id = 0)
  if (!selectedIds.includes(0)) {
    
    // Lấy các trạng thái đặc biệt: Bỏ ID 1 (Chưa xác định), Bỏ ID 2 (Bình thường)
    // Sắp xếp ĐỘ DÀI TÊN GIẢM DẦN để check chữ dài ("Like New") trước chữ ngắn ("New")
    const sortedSpecialStatuses = statusList.value
      .filter(s => s.id > 2) 
      .sort((a, b) => b.name.length - a.name.length);

    filtered = filtered.filter(item => {
      
      // Nếu Backend trả về chuẩn mã trạng thái -> Dùng luôn
      if (item.maTrangThai !== undefined && item.maTrangThai !== null) {
        return selectedIds.includes(item.maTrangThai);
      }

      // Nếu Backend thiếu mã -> Lọc theo tên hiển thị
      const tenSPNoSpace = item.tenSP ? item.tenSP.toLowerCase().replace(/\s+/g, '') : '';
      
      let matchedStatusId = 2; // Mặc định ID 2 là "Bình Thường"

      // Dò tìm đuôi trạng thái
      for (const st of sortedSpecialStatuses) {
        const kwNoSpace = st.name.toLowerCase().replace(/\s+/g, '');
        
        if (tenSPNoSpace.includes(kwNoSpace)) {
          matchedStatusId = st.id; 
          break; // Bắt được Like New là thoát vòng lặp ngay, không check chữ New nữa
        }
      }

      // Cuối cùng: So sánh xem ID tìm được có nằm trong danh sách đang tick chọn không
      return selectedIds.includes(matchedStatusId);
    });
  }

  filteredHistoryData.value = filtered;
  grandTotalHistory.value = calculateTotals(filteredHistoryData.value);
  historyPagination.total = filteredHistoryData.value.length;
  historyPagination.totalPages = Math.ceil(filteredHistoryData.value.length / historyPagination.size);
  historyPagination.page = 0;
  applyHistoryPagination();
};

const calculateTotals = (dataArr) => {
  return dataArr.reduce((acc, item) => {
    acc.tdk += Number(item.tonDau || 0);
    acc.ntk += Number(item.nhapTrong || 0);
    acc.xtk += Number(item.xuatTrong || 0);
    acc.tck += Number(item.tonCuoi || 0);
    acc.tien += Number(item.thanhTien || 0);
    return acc;
  }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });
};

const applyHistoryPagination = () => {
  const start = historyPagination.page * historyPagination.size;
  historyData.value = filteredHistoryData.value.slice(start, start + historyPagination.size);
};

const changeHistoryPage = (newPage) => {
  if (newPage >= 0 && newPage < historyPagination.totalPages) {
    historyPagination.page = newPage;
    applyHistoryPagination();
  }
};

// 6. XUẤT FILE & IN ẤN
const printReport = (namInput, tenKhoString, tabName) => {
  const printWindow = window.open('', '_blank');
  if (!printWindow) return Swal.fire('Lưu ý', 'Vui lòng cho phép popup trên trình duyệt để in báo cáo.', 'warning');

  const sourceData = tabName === 'action' ? allActionData.value : filteredHistoryData.value;
  const sum = tabName === 'action' ? grandTotalAction.value : grandTotalHistory.value;
  const tenKho = tenKhoString || "Kho hệ thống";

  let html = `<html><head><title>In Báo Cáo Chốt Sổ</title><style>body { font-family: Arial, sans-serif; padding: 20px; color: #000; } h2 { text-align: center; margin-bottom: 5px; } .info-text { text-align: center; margin-bottom: 20px; font-size: 14px; } table { width: 100%; border-collapse: collapse; font-size: 12px; } th, td { border: 1px solid #000; padding: 6px 8px; } th { background-color: #f2f2f2; text-align: center; font-weight: bold; } .text-center { text-align: center; } .text-right { text-align: right; } .text-left { text-align: left; } .font-weight-bold { font-weight: bold; } .total-row { background-color: #e9ecef; font-weight: bold; } @media print { @page { size: landscape; margin: 10mm; } body { -webkit-print-color-adjust: exact; padding: 0; } }</style></head><body><h2>BIÊN BẢN CHỐT TỒN KHO</h2><div class="info-text"><strong>Năm chốt sổ:</strong> ${namInput} <br/><strong>Kho/Chi nhánh:</strong> ${tenKho}</div><table><thead><tr><th>STT</th><th>Mã SP</th><th>Tên Sản Phẩm</th><th>ĐVT</th><th>Tồn Đầu</th><th>Nhập</th><th>Xuất</th><th>Tồn Cuối</th><th>Giá BQ</th><th>Thành Tiền</th></tr></thead><tbody>`;

  sourceData.forEach((item, index) => {
    html += `<tr><td class="text-center">${index + 1}</td><td class="text-left font-weight-bold">${item.maSP}</td><td class="text-left">${formatProductName(item.tenSP)}</td><td class="text-center">${item.donvitinh}</td><td class="text-right">${item.tonDau || 0}</td><td class="text-right">${item.nhapTrong || 0}</td><td class="text-right">${item.xuatTrong || 0}</td><td class="text-right font-weight-bold">${item.tonCuoi || 0}</td><td class="text-right">${formatCurrency(item.giaBQ || 0)}</td><td class="text-right font-weight-bold">${formatCurrency(item.thanhTien || 0)}</td></tr>`;
  });

  html += `</tbody><tfoot><tr class="total-row"><td colspan="4" class="text-center">TỔNG CỘNG</td><td class="text-right">${sum.tdk}</td><td class="text-right">${sum.ntk}</td><td class="text-right">${sum.xtk}</td><td class="text-right">${sum.tck}</td><td class="text-right"></td><td class="text-right">${formatCurrency(sum.tien)}</td></tr></tfoot></table><div style="margin-top: 30px; display: flex; justify-content: space-around; font-size: 14px;"><div style="text-align: center;"><strong>Người lập bảng</strong><br><br><br>(Ký, họ tên)</div><div style="text-align: center;"><strong>Kế toán trưởng</strong><br><br><br>(Ký, họ tên)</div><div style="text-align: center;"><strong>Giám đốc</strong><br><br><br>(Ký, họ tên, đóng dấu)</div></div></body></html>`;

  printWindow.document.write(html);
  printWindow.document.close();
  printWindow.focus();
  setTimeout(() => { printWindow.print(); printWindow.close(); }, 250);
};

const printToWord = async (namInput, maKhoInput, tenKhoString, tabName) => {
  if (isExporting.value) return;
  isExporting.value = true;
  try {
    const sourceData = tabName === 'action' ? allActionData.value : filteredHistoryData.value;
    const totals = tabName === 'action' ? grandTotalAction.value : grandTotalHistory.value;
    const tenKho = tenKhoString || "Kho hệ thống";

    const content = await (await fetch("/File_Mau_BaoCaoChotSoNam.docx")).arrayBuffer();
    const doc = new Docxtemplater(new PizZip(content), { paragraphLoop: true, linebreaks: true });

    const today = new Date();
    doc.render({
      nam: parseInt(namInput), tenKho: tenKho,
      d: String(today.getDate()).padStart(2, '0'), m: String(today.getMonth() + 1).padStart(2, '0'), y: today.getFullYear(),
      h: String(today.getHours() % 12 || 12).padStart(2, '0'), ph: String(today.getMinutes()).padStart(2, '0'), ampm: today.getHours() >= 12 ? 'PM' : 'AM',
      sumTDK: formatCurrency(totals.tdk), sumNTK: formatCurrency(totals.ntk), sumXTK: formatCurrency(totals.xtk), sumTCK: formatCurrency(totals.tck), sumTien: formatCurrency(totals.tien),
      p: sourceData.map((item, index) => ({
        stt: index + 1, ma: item.maSP || "", ten: formatProductName(item.tenSP) || "", dvt: item.donvitinh || "",
        tdk: formatCurrency(item.tonDau || 0), ntk: formatCurrency(item.nhapTrong || 0), xtk: formatCurrency(item.xuatTrong || 0),
        tck: formatCurrency(item.tonCuoi || 0), gia: formatCurrency(item.giaBQ || 0), tien: formatCurrency(item.thanhTien || 0)
      }))
    });

    saveAs(doc.getZip().generate({ type: "blob", mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document" }), `BienBanChotSo_${tenKho.replace(/\s+/g, '_')}_Nam_${parseInt(namInput)}.docx`);
  } catch (error) { Swal.fire('Lỗi', "Có lỗi xảy ra khi xuất file Word: " + error.message, 'error'); }
  finally { isExporting.value = false; }
};

const exportToExcel = async (namInput, maKhoInput, tenKhoString, tabName) => {
  if (isExportingExcel.value) return;
  isExportingExcel.value = true;
  try {
    const sourceData = tabName === 'action' ? allActionData.value : filteredHistoryData.value;
    const totals = tabName === 'action' ? grandTotalAction.value : grandTotalHistory.value;
    const tenKho = tenKhoString || "Kho hệ thống";

    const workbook = new ExcelJS.Workbook();
    const sheet = workbook.addWorksheet('Biên Bản Chốt Sổ');

    sheet.mergeCells('A1:J1');
    sheet.getCell('A1').value = 'BIÊN BẢN CHỐT TỒN KHO';
    sheet.getCell('A1').font = { size: 16, bold: true };
    sheet.getCell('A2').value = `Năm chốt sổ:`; sheet.getCell('C2').value = parseInt(namInput);
    sheet.getCell('A3').value = `Kho:`; sheet.getCell('C3').value = tenKho; sheet.getCell('C3').font = { bold: true };

    const headerRow = sheet.addRow(['STT', 'Mã sản phẩm', 'Tên sản phẩm', 'ĐVT', 'Tồn Đầu', 'Nhập', 'Xuất', 'Tồn Cuối', 'Giá/BQ', 'Thành tiền']);
    headerRow.font = { bold: true }; headerRow.alignment = { vertical: 'middle', horizontal: 'center' };

    sheet.columns = [
      { key: 'stt', width: 5 }, { key: 'maSP', width: 20 }, { key: 'tenSP', width: 45 }, { key: 'dvt', width: 8 },
      { key: 'tdk', width: 10 }, { key: 'ntk', width: 10 }, { key: 'xtk', width: 10 }, { key: 'tck', width: 10 }, { key: 'gia', width: 15 }, { key: 'tien', width: 18 }
    ];

    sourceData.forEach((item, index) => {
      const row = sheet.addRow([
        index + 1, item.maSP, formatProductName(item.tenSP), item.donvitinh,
        item.tonDau || 0, item.nhapTrong || 0, item.xuatTrong || 0, item.tonCuoi || 0, item.giaBQ || 0, item.thanhTien || 0
      ]);
      for (let i = 5; i <= 10; i++) row.getCell(i).alignment = { horizontal: 'right' };
    });

    const totalRow = sheet.addRow(['Tổng Cộng', '', '', '', totals.tdk, totals.ntk, totals.xtk, totals.tck, '', totals.tien]);
    totalRow.font = { bold: true }; sheet.mergeCells(`A${totalRow.number}:D${totalRow.number}`); totalRow.getCell(1).alignment = { horizontal: 'center' };
    for (let i = 5; i <= 10; i++) totalRow.getCell(i).alignment = { horizontal: 'right' };

    sheet.eachRow((row, rowNumber) => {
      if (rowNumber >= 4 && rowNumber <= totalRow.number) {
        row.eachCell((cell) => cell.border = { top: { style: 'thin' }, left: { style: 'thin' }, bottom: { style: 'thin' }, right: { style: 'thin' } });
      }
    });

    const buffer = await workbook.xlsx.writeBuffer();
    saveAs(new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }), `BienBanChotSo_${tenKho.replace(/\s+/g, '_')}_Nam_${parseInt(namInput)}.xlsx`);
  } catch (error) { Swal.fire('Lỗi', "Có lỗi xảy ra khi xuất file Excel.", 'error'); }
  finally { isExportingExcel.value = false; }
};

// 7. KHỞI TẠO (LIFECYCLE & EVENT LISTENERS)
const toggleStatusDropdown = () => isStatusDropdownOpen.value = !isStatusDropdownOpen.value;
const closeDropdownOutside = (e) => {
  if (statusDropdownRef.value && !statusDropdownRef.value.contains(e.target)) isStatusDropdownOpen.value = false;
};

const setupPhanQuyen = () => {
  const role = localStorage.getItem('userRole');
  let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
  if (!userMaKho) {
    try { userMaKho = JSON.parse(localStorage.getItem('userInfo') || '{}').maKho; } catch (e) { }
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

const loadKho = async () => {
  try {
    const res = await api.get('/kho');
    khoList.value = res.data;
    if (res.data.length > 0) {
      if (!filters.warehouseId || filters.warehouseId === '') filters.warehouseId = res.data[0].maKho;
      if (!historyFilters.warehouseId || historyFilters.warehouseId === '') historyFilters.warehouseId = res.data[0].maKho;
    }
  } catch (e) { console.error("Lỗi tải kho:", e); }
};

const loadTrangThai = async () => {
  try {
    const res = await api.get('/trang-thai');
    statusList.value = [{ id: 0, name: 'Tất cả' }, ...res.data.map(item => ({ id: item.maTrangThai, name: item.tenTrangThai }))];
  } catch (e) { console.error("Lỗi tải trạng thái:", e); }
};

onMounted(async () => {
  if (!localStorage.getItem('token')) return;
  setupPhanQuyen();
  await Promise.all([loadKho(), loadTrangThai()]);
  document.addEventListener('click', closeDropdownOutside);
});

onUnmounted(() => document.removeEventListener('click', closeDropdownOutside));
</script>

<style scoped>
/* CODE CSS ĐƯỢC GIỮ NGUYÊN 100% CỦA BẠN */
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

.custom-multi-select {
  position: relative;
}

.dropdown-trigger {
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  user-select: none;
}

.dropdown-trigger.is-focused {
  color: #495057;
  background-color: #fff;
  border-color: #80bdff;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, .25);
}

.dropdown-text {
  flex-grow: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: inherit;
  color: #000000;
}

.dropdown-icon {
  font-size: 10px;
  color: #6c757d;
  margin-left: 8px;
}

.dropdown-menu-custom {
  position: absolute;
  top: calc(100% - 2px);
  left: 0;
  z-index: 1050;
  width: 100%;
  background-color: #fff;
  border: 1px solid #80bdff;
  border-bottom-left-radius: 0.25rem;
  border-bottom-right-radius: 0.25rem;
}

.dropdown-list {
  max-height: 250px;
  overflow-y: auto;
  padding: 4px 0;
}

.dropdown-item-custom {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  margin: 0;
  cursor: pointer;
  font-weight: 400;
  font-size: 1rem;
  color: #000000;
  transition: background-color 0.15s;
  gap: 8px;
}

.dropdown-item-custom:hover {
  background-color: #f8f9fa;
}

.checkbox-ui {
  width: 15px;
  height: 15px;
  margin: 0;
  cursor: pointer;
}

.dropdown-list::-webkit-scrollbar {
  width: 6px;
}

.dropdown-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.dropdown-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
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