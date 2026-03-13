<template>
  <div class="content-wrapper">
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6 d-flex align-items-center">
            <h1 class="m-0">Báo Cáo Xuất Nhập Tồn</h1>
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

        <div class="card card-primary card-outline">
          <div class="card-header">
            <h3 class="card-title"><i class="fas fa-filter"></i> Bộ lọc báo cáo</h3>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label>Từ ngày</label>
                  <input type="date" class="form-control" v-model="filters.startDate">
                </div>
              </div>
              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label>Đến ngày</label>
                  <input type="date" class="form-control" v-model="filters.endDate">
                </div>
              </div>
              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group">
                  <label>Kho/Chi nhánh</label>
                  <select class="form-control" v-model="filters.warehouseId" :disabled="!isAdmin" @change="handleSearch">
                    <option v-for="k in khoList" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                  </select>
                </div>
              </div>

              <div class="col-lg-3 col-md-6 col-12">
                <div class="form-group custom-multi-select" ref="statusDropdownRef">
                  <label>Trạng thái máy</label>
                  <div class="form-control dropdown-trigger" :class="{ 'is-focused': isStatusDropdownOpen }" @click="toggleStatusDropdown">
                    <span class="dropdown-text">{{ selectedStatusText }}</span>
                    <i class="fas fa-chevron-down dropdown-icon"></i>
                  </div>

                  <div class="dropdown-menu-custom shadow-sm" v-show="isStatusDropdownOpen">
                    <div class="dropdown-list">
                      <label class="dropdown-item-custom" v-for="st in statusList" :key="st.id">
                        <input type="checkbox" class="checkbox-ui" :value="st.id" v-model="filters.statusIds" @change="handleStatusChange(st.id)">
                        <span class="ml-2">{{ st.name }}</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>
          <div class="card-footer">
            <button type="button" class="btn btn-primary" @click="handleSearch" :disabled="loading">
              <i class="fas" :class="loading ? 'fa-spinner fa-spin' : 'fa-search'"></i>
              {{ loading ? 'Đang tải...' : 'Xem Báo Cáo' }}
            </button>
          </div>
        </div>

        <div class="d-flex justify-content-end my-3">
            <button type="button" class="btn btn-sm btn-dark mr-2" @click="printReport" :disabled="loading || allReportData.length === 0"> 
                <i class="fas fa-print"></i> In Báo Cáo
            </button>
            <button type="button" class="btn btn-sm btn-info mr-2" @click="printToWord" :disabled="isExporting || allReportData.length === 0"> 
                <i class="fas" :class="isExporting ? 'fa-spinner fa-spin' : 'fa-file-word'"></i> {{ isExporting ? 'Đang xuất...' : 'In File Word' }}
            </button>
            <button type="button" class="btn btn-sm btn-success" @click="exportToExcel" :disabled="isExportingExcel || allReportData.length === 0">
                <i class="fas" :class="isExportingExcel ? 'fa-spinner fa-spin' : 'fa-file-excel'"></i> {{ isExportingExcel ? 'Đang xuất...' : 'Xuất Excel' }}
            </button>
        </div>

        <div class="card">
          <div class="card-header border-0">
            <h3 class="card-title text-success font-weight-bold" style="margin-top: 0.3rem;">
              <i class="fas fa-chart-bar"></i> {{ reportTitle }}
              <span class="badge badge-success ml-2" v-if="filteredReportData.length > 0">Tổng: {{ filteredReportData.length }} dòng</span>
            </h3>
          </div>

          <div class="card-body table-responsive p-0">
            <table class="table table-bordered table-striped table-hover text-nowrap">
              <thead class="bg-light">
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
                <template v-if="loading">
                  <tr v-for="n in 5" :key="`skel-${n}`">
                    <td v-for="m in 10" :key="m">
                      <div class="skeleton-loader"></div>
                    </td>
                  </tr>
                </template>

                <template v-else>
                  <tr v-if="paginatedData.length === 0">
                    <td colspan="10" class="text-center py-4 text-muted">
                      <i class="fas fa-inbox fa-2x mb-2"></i><br>
                      Không có dữ liệu phát sinh trong khoảng thời gian này.
                    </td>
                  </tr>

                  <tr v-for="(item, index) in paginatedData" :key="index">
                    <td class="text-center">
                      {{ (pagination.page * pagination.size) + index + 1 }}
                    </td>
                    <td class="text-primary font-weight-bold font-monospace">{{ item.maSP }}</td>
                    <td>{{ formatProductName(item.tenSP) }}</td>
                    <td class="text-center">{{ item.donvitinh }}</td>

                    <td class="text-right">{{ item.tonDau || 0 }}</td>
                    <td class="text-right text-success">+{{ item.nhapTrong || 0 }}</td>
                    <td class="text-right text-danger">-{{ item.xuatTrong || 0 }}</td>
                    <td class="text-right font-weight-bold bg-warning bg-opacity-25">{{ item.tonCuoi || 0 }}</td>
                    <td class="text-right">{{ formatCurrency(item.giaBQ || 0) }}</td>
                    <td class="text-right font-weight-bold text-primary">{{ formatCurrency(item.thanhTien || 0) }}</td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>

          <div class="bg-light border-top py-1" v-if="filteredReportData.length > 0 && !loading"
            style="border-bottom-left-radius: .25rem; border-bottom-right-radius: .25rem;">
            <div class="d-flex justify-content-around text-center align-items-center"
              style="font-size: 13px; min-height: 40px;">
              <div class="flex-fill border-right">
                <span class="text-muted mr-1">Tồn Đầu:</span>
                <span class="font-weight-bold">{{ grandTotal.tdk }}</span>
              </div>
              <div class="flex-fill border-right">
                <span class="text-success mr-1">Nhập:</span>
                <span class="font-weight-bold">{{ grandTotal.ntk }}</span>
              </div>
              <div class="flex-fill border-right">
                <span class="text-danger mr-1">Xuất:</span>
                <span class="font-weight-bold">{{ grandTotal.xtk }}</span>
              </div>
              <div class="flex-fill border-right">
                <span class="text-dark mr-1">Tồn Cuối:</span>
                <span class="font-weight-bold text-warning">{{ grandTotal.tck }}</span>
              </div>
              <div class="flex-fill">
                <span class="text-primary mr-1">Tổng Tiền:</span>
                <span class="font-weight-bold text-primary">{{ formatCurrency(grandTotal.tien) }}</span>
              </div>
            </div>
          </div>

          <div class="d-flex justify-content-center mt-2 px-3 pb-3" v-if="pagination.totalPages > 1">
            <ul class="pagination pagination-sm m-0">
              <li class="page-item" :class="{ disabled: pagination.page === 0 }">
                <a class="page-link" href="#" @click.prevent="changePage(pagination.page - 1)">« Trước</a>
              </li>
              <li v-for="(page, index) in visiblePages" :key="index" class="page-item"
                :class="{ active: page === pagination.page + 1, disabled: page === '...' }">

                <a class="page-link" href="#" @click.prevent="page !== '...' ? changePage(page - 1) : null">
                  {{ page }}
                </a>
              </li>

              <li class="page-item" :class="{ disabled: pagination.page >= pagination.totalPages - 1 }">
                <a class="page-link" href="#" @click.prevent="changePage(pagination.page + 1)">Sau »</a>
              </li>
            </ul>
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

const API_URL = '/thong-ke/xuat-nhap-ton';
const today = new Date();
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);

// ==========================================
// 1. QUẢN LÝ TRẠNG THÁI (STATE)
// ==========================================
const loading = ref(false);
const isExporting = ref(false);
const isExportingExcel = ref(false);
const isAdmin = ref(false);
const isChotSoState = ref(true);

const allReportData = ref([]); 
const filteredReportData = ref([]); 
const khoList = ref([]);
const statusList = ref([{ id: 0, name: 'Tất cả' }]);
const currentTenKho = ref('');

const pagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });
const filters = reactive({
  startDate: firstDay.toISOString().substring(0, 10),
  endDate: today.toISOString().substring(0, 10),
  warehouseId: '', 
  statusIds: [0] 
});

const isStatusDropdownOpen = ref(false);
const statusDropdownRef = ref(null);

// ==========================================
// 2. DỮ LIỆU TÍNH TOÁN TỰ ĐỘNG (COMPUTED)
// ==========================================
const paginatedData = computed(() => {
  const start = pagination.page * pagination.size;
  return filteredReportData.value.slice(start, start + pagination.size);
});

const grandTotal = computed(() => {
  return filteredReportData.value.reduce((acc, item) => {
    acc.tdk += Number(item.tonDau || 0);
    acc.ntk += Number(item.nhapTrong || 0);
    acc.xtk += Number(item.xuatTrong || 0);
    acc.tck += Number(item.tonCuoi || 0);
    acc.tien += Number(item.thanhTien || 0);
    return acc;
  }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });
});

const visiblePages = computed(() => {
  const total = pagination.totalPages;
  if (total === 0) return [];
  const current = pagination.page + 1;
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
});

const reportTitle = computed(() => {
  return `Báo cáo: ${currentTenKho.value || "Kho hệ thống"} (${formatDateString(filters.startDate)} - ${formatDateString(filters.endDate)})`;
});

const selectedStatusText = computed(() => {
  if (filters.statusIds.includes(0)) return 'Tất cả trạng thái';
  return statusList.value.filter(s => filters.statusIds.includes(s.id)).map(s => s.name).join(', ');
});

// ==========================================
// 3. CÁC HÀM FORMAT TIỆN ÍCH
// ==========================================
const formatProductName = (name) => {
  if (!name) return '';
  return name.toString()
    .replace(/\s*-\s*Bình thường/gi, '')
    .replace(/\s*-\s*Chưa xác định/gi, '')
    .replace(/\s*-\s*Mới\s*\(New\)/gi, ' - New')
    .replace(/\s*-\s*Mới/gi, ' - New')
    .replace(/\s*-\s*New/gi, ' - New') 
    .replace(/\s*-\s*$/g, '')
    .trim();
};

const formatCurrency = (value) => {
  if (value === null || value === undefined || isNaN(value)) return '0';
  return new Intl.NumberFormat('vi-VN', { style: 'decimal', decimal: 'VND' }).format(value);
};

const formatDateString = (dateStr) => {
  if (!dateStr) return "...";
  const parts = dateStr.split('-');
  return parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : dateStr;
};

// ==========================================
// 4. LOGIC TẢI DỮ LIỆU & BỘ LỌC
// ==========================================
const handleSearch = () => {
  if (!filters.warehouseId) return; 
  pagination.page = 0;
  fetchInventoryReport();
};

const fetchInventoryReport = async () => {
  if (!filters.startDate || !filters.endDate) return alert("Vui lòng chọn đầy đủ Từ ngày và Đến ngày");
  if (new Date(filters.startDate) > new Date(filters.endDate)) return alert("Ngày bắt đầu không được lớn hơn ngày kết thúc");

  loading.value = true;
  allReportData.value = [];
  filteredReportData.value = [];

  try {
    const yearToCheck = new Date(filters.startDate).getFullYear();
    const checkRes = await api.get('/thong-ke/check-chot-so', { params: { nam: yearToCheck, maKho: filters.warehouseId } });
    isChotSoState.value = checkRes.data;

    const response = await api.get(API_URL, {
      params: { maKho: filters.warehouseId, tuNgay: filters.startDate, denNgay: filters.endDate, loaiLoc: 0, page: 0, size: 999999 }
    });

    currentTenKho.value = response.data.tenKho;
    allReportData.value = response.data.danhSachChiTiet || [];
    applyStatusFilter();
  } catch (error) {
    if (error.response && error.response.status === 400) alert("Lỗi dữ liệu: " + error.response.data);
    else alert("Có lỗi xảy ra khi tải báo cáo.");
  } finally {
    loading.value = false;
  }
};

const handleStatusChange = (changedId) => {
  if (changedId === 0 && filters.statusIds.includes(0)) filters.statusIds = [0];
  else if (changedId !== 0 && filters.statusIds.includes(0)) filters.statusIds = filters.statusIds.filter(id => id !== 0);
  if (filters.statusIds.length === 0) filters.statusIds = [0];
  applyStatusFilter();
};

const applyStatusFilter = () => {
  let filtered = allReportData.value;
  const selectedIds = filters.statusIds;

  if (!selectedIds.includes(0)) {
    const otherSelectedNames = statusList.value.filter(s => selectedIds.includes(s.id) && s.id > 1).map(s => s.name);
    const hasNormal = selectedIds.includes(1);
    const allOtherNames = statusList.value.filter(s => s.id > 1).map(s => s.name);

    filtered = filtered.filter(item => {
      const isNormalMachine = !allOtherNames.some(ext => item.tenSP.endsWith(' - ' + ext));
      if (hasNormal && isNormalMachine) return true;
      return otherSelectedNames.some(ext => item.tenSP.endsWith(' - ' + ext));
    });
  }

  filteredReportData.value = filtered;
  pagination.total = filteredReportData.value.length;
  pagination.totalPages = Math.ceil(pagination.total / pagination.size);
  pagination.page = 0;
};

// ==========================================
// 5. LOGIC UI (Dropdown, Phân trang, Cảnh báo)
// ==========================================
const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) pagination.page = newPage;
};

const toggleStatusDropdown = () => isStatusDropdownOpen.value = !isStatusDropdownOpen.value;
const closeDropdownOutside = (e) => {
  if (statusDropdownRef.value && !statusDropdownRef.value.contains(e.target)) isStatusDropdownOpen.value = false;
};

const showFirstYearWarning = () => alert("LƯU Ý:\nĐây là năm đầu tiên sử dụng phần mềm, hệ thống chưa có dữ liệu chốt sổ của năm trước.\n\nDo đó, Tồn Đầu Kỳ sẽ được mặc định là 0.");

// ==========================================
// 6. XUẤT FILE & IN ẤN (IN, WORD, EXCEL)
// ==========================================
const printReport = () => {
  const printWindow = window.open('', '_blank');
  if (!printWindow) return alert("Vui lòng cho phép popup trên trình duyệt để in báo cáo.");
  
  const data = filteredReportData.value;
  const sum = grandTotal.value;
  
  let html = `
    <html>
    <head>
      <title>In Báo Cáo Tồn Kho</title>
      <style>
        body { font-family: Arial, sans-serif; padding: 20px; color: #000; }
        h2 { text-align: center; margin-bottom: 5px; }
        .info-text { text-align: center; margin-bottom: 20px; font-size: 14px; }
        table { width: 100%; border-collapse: collapse; font-size: 12px; }
        th, td { border: 1px solid #000; padding: 6px 8px; }
        th { background-color: #f2f2f2; text-align: center; font-weight: bold; }
        .text-center { text-align: center; }
        .text-right { text-align: right; }
        .text-left { text-align: left; }
        .font-weight-bold { font-weight: bold; }
        .total-row { background-color: #e9ecef; font-weight: bold; }
        @media print { @page { size: landscape; margin: 10mm; } body { -webkit-print-color-adjust: exact; padding: 0; } }
      </style>
    </head>
    <body>
      <h2>BÁO CÁO XUẤT NHẬP TỒN</h2>
      <div class="info-text">
        <strong>Thời gian:</strong> Từ ${formatDateString(filters.startDate)} đến ${formatDateString(filters.endDate)} <br/>
        <strong>Kho/Chi nhánh:</strong> ${currentTenKho.value}
      </div>
      <table>
        <thead>
          <tr>
            <th>STT</th>
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
  `;
  
  data.forEach((item, index) => {
    html += `
      <tr>
        <td class="text-center">${index + 1}</td>
        <td class="text-left font-weight-bold">${item.maSP}</td>
        <td class="text-left">${formatProductName(item.tenSP)}</td>
        <td class="text-center">${item.donvitinh}</td>
        <td class="text-right">${item.tonDau || 0}</td>
        <td class="text-right">${item.nhapTrong || 0}</td>
        <td class="text-right">${item.xuatTrong || 0}</td>
        <td class="text-right font-weight-bold">${item.tonCuoi || 0}</td>
        <td class="text-right">${formatCurrency(item.giaBQ || 0)}</td>
        <td class="text-right font-weight-bold">${formatCurrency(item.thanhTien || 0)}</td>
      </tr>
    `;
  });
  
  html += `
        </tbody>
        <tfoot>
          <tr class="total-row">
            <td colspan="4" class="text-center">TỔNG CỘNG</td>
            <td class="text-right">${sum.tdk}</td>
            <td class="text-right">${sum.ntk}</td>
            <td class="text-right">${sum.xtk}</td>
            <td class="text-right">${sum.tck}</td>
            <td class="text-right"></td>
            <td class="text-right">${formatCurrency(sum.tien)}</td>
          </tr>
        </tfoot>
      </table>
      <div style="margin-top: 30px; display: flex; justify-content: space-around; font-size: 14px;">
        <div style="text-align: center;"><strong>Người lập bảng</strong><br><br><br>(Ký, họ tên)</div>
        <div style="text-align: center;"><strong>Kế toán trưởng</strong><br><br><br>(Ký, họ tên)</div>
        <div style="text-align: center;"><strong>Giám đốc</strong><br><br><br>(Ký, họ tên, đóng dấu)</div>
      </div>
    </body>
    </html>
  `;
  
  printWindow.document.write(html);
  printWindow.document.close();
  printWindow.focus();
  setTimeout(() => { printWindow.print(); printWindow.close(); }, 250);
};

const printToWord = async () => {
  if (isExporting.value) return;
  isExporting.value = true;
  try {
    const dataToExport = filteredReportData.value;
    if (dataToExport.length === 0) return alert("Không có dữ liệu để xuất file.");

    const content = await (await fetch("/File_Mau_BaoCaoTonKho.docx")).arrayBuffer();
    const doc = new Docxtemplater(new PizZip(content), { paragraphLoop: true, linebreaks: true });

    const now = new Date();
    doc.render({
      ngayBatDau: formatDateString(filters.startDate), ngayKetThuc: formatDateString(filters.endDate), tenKho: currentTenKho.value,
      sumTDK: grandTotal.value.tdk, sumNTK: grandTotal.value.ntk, sumXTK: grandTotal.value.xtk, sumTCK: grandTotal.value.tck, sumTien: formatCurrency(grandTotal.value.tien),
      d: String(now.getDate()).padStart(2, '0'), m: String(now.getMonth() + 1).padStart(2, '0'), y: now.getFullYear(),
      h: String(now.getHours() % 12 || 12).padStart(2, '0'), ph: String(now.getMinutes()).padStart(2, '0'), ampm: now.getHours() >= 12 ? 'PM' : 'AM',
      p: dataToExport.map((item, i) => ({
        stt: i + 1, ma: item.maSP || "", ten: formatProductName(item.tenSP) || "", dvt: item.donvitinh || "",
        tdk: item.tonDau || 0, ntk: item.nhapTrong || 0, xtk: item.xuatTrong || 0, tck: item.tonCuoi || 0,
        gia: formatCurrency(item.giaBQ || 0), tien: formatCurrency(item.thanhTien || 0)
      }))
    });

    saveAs(doc.getZip().generate({ type: "blob", mimeType: "application/vnd.openxmlformats-officedocument.wordprocessingml.document" }), `Báo Cáo Tồn Kho - ${currentTenKho.value} _ ${formatDateString(filters.endDate).replace(/\//g, '-')}.docx`);
  } catch (error) { alert("Lỗi xuất file Word: " + error.message); } 
  finally { isExporting.value = false; }
};

const exportToExcel = async () => {
  if (isExportingExcel.value) return;
  isExportingExcel.value = true;
  try {
    const dataToExport = filteredReportData.value;
    if (dataToExport.length === 0) return alert("Không có dữ liệu để xuất file.");

    const workbook = new ExcelJS.Workbook();
    const sheet = workbook.addWorksheet('Báo Cáo Tồn Kho');

    sheet.mergeCells('A1:J1');
    sheet.getCell('A1').value = 'BÁO CÁO XUẤT NHẬP TỒN';
    sheet.getCell('A1').font = { size: 16, bold: true };
    sheet.getCell('A2').value = 'Ngày bắt đầu:'; sheet.getCell('C2').value = formatDateString(filters.startDate);
    sheet.getCell('A3').value = 'Ngày kết thúc:'; sheet.getCell('C3').value = formatDateString(filters.endDate);
    sheet.getCell('A4').value = `Kho: ${currentTenKho.value}`; sheet.getCell('A4').font = { bold: true };

    const headerRow = sheet.addRow(['Stt', 'Mã sản phẩm', 'Tên sản phẩm', 'ĐVT', 'TĐK', 'NTK', 'XTK', 'TCK', 'Giá/BQ', 'Thành tiền']);
    headerRow.font = { bold: true }; headerRow.alignment = { vertical: 'middle', horizontal: 'center' };

    sheet.columns = [
      { key: 'stt', width: 5 }, { key: 'maSP', width: 20 }, { key: 'tenSP', width: 45 }, { key: 'dvt', width: 8 },
      { key: 'tdk', width: 10 }, { key: 'ntk', width: 10 }, { key: 'xtk', width: 10 }, { key: 'tck', width: 10 }, { key: 'gia', width: 15 }, { key: 'tien', width: 18 }
    ];

    dataToExport.forEach((item, index) => {
      const row = sheet.addRow([
        index + 1, item.maSP, formatProductName(item.tenSP), item.donvitinh,
        item.tonDau || 0, item.nhapTrong || 0, item.xuatTrong || 0, item.tonCuoi || 0, item.giaBQ || 0, item.thanhTien || 0
      ]);
      for (let i = 5; i <= 10; i++) row.getCell(i).alignment = { horizontal: 'right' };
    });

    const totalRow = sheet.addRow(['Tổng', '', '', '', grandTotal.value.tdk, grandTotal.value.ntk, grandTotal.value.xtk, grandTotal.value.tck, '', grandTotal.value.tien]);
    totalRow.font = { bold: true }; sheet.mergeCells(`A${totalRow.number}:D${totalRow.number}`);

    const sumQtyRow = sheet.addRow([`Tổng số lượng: ${grandTotal.value.tck} Cái`]);
    sumQtyRow.font = { bold: true }; sheet.mergeCells(`A${sumQtyRow.number}:J${sumQtyRow.number}`);

    const dateRow = sheet.addRow([`Ngày in ${String(new Date().getDate()).padStart(2, '0')}-${String(new Date().getMonth() + 1).padStart(2, '0')}-${new Date().getFullYear()}`]);
    dateRow.alignment = { horizontal: 'right' }; sheet.mergeCells(`A${dateRow.number}:J${dateRow.number}`);

    sheet.eachRow((row, rowNumber) => {
      if (rowNumber >= 5 && rowNumber <= totalRow.number) {
        row.eachCell(cell => cell.border = { top: { style: 'thin' }, left: { style: 'thin' }, bottom: { style: 'thin' }, right: { style: 'thin' } });
      }
    });

    const buffer = await workbook.xlsx.writeBuffer();
    saveAs(new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }), `Báo Cáo Xuất Nhập Tồn _ ${currentTenKho.value.replace(/\s+/g, '')} _ ${formatDateString(filters.endDate).replace(/\//g, '-')}.xlsx`);
  } catch (error) { alert("Có lỗi xảy ra khi xuất file Excel."); } 
  finally { isExportingExcel.value = false; }
};

// ==========================================
// 7. KHỞI TẠO (LIFECYCLE)
// ==========================================
const setupPhanQuyen = () => {
  const role = localStorage.getItem('userRole');
  let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
  if (!userMaKho) {
    try { userMaKho = JSON.parse(localStorage.getItem('userInfo') || '{}').maKho; } catch (e) { }
  }
  if (role === 'ADMIN' || role === 'ROLE_ADMIN') isAdmin.value = true;
  else { isAdmin.value = false; filters.warehouseId = userMaKho ? parseInt(userMaKho) : ''; }
};

const loadKho = async () => {
  try {
    const res = await api.get('/kho');
    khoList.value = res.data;
    if (res.data.length > 0 && (!filters.warehouseId || filters.warehouseId === '')) filters.warehouseId = res.data[0].maKho;
  } catch (e) { console.error("Lỗi tải kho:", e); }
};

const loadTrangThai = async () => {
  try {
    const res = await api.get('/trang-thai');
    statusList.value = [{ id: 0, name: 'Tất cả' }, ...res.data.map(i => ({ id: i.maTrangThai, name: i.tenTrangThai }))];
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
/* CODE CSS CỦA BẠN ĐƯỢC GIỮ NGUYÊN HOÀN TOÀN TỪ ĐẦU ĐẾN CUỐI */
.custom-multi-select { position: relative; }
.dropdown-trigger { cursor: pointer; display: flex; justify-content: space-between; align-items: center; background-color: #fff; user-select: none; }
.dropdown-trigger.is-focused { color: #495057; background-color: #fff; border-color: #80bdff; outline: 0; box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25); }
.dropdown-text { flex-grow: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-size: inherit; color: #000000; }
.dropdown-icon { font-size: 10px; color: #6c757d; margin-left: 8px; }
.dropdown-menu-custom { position: absolute; top: calc(100% - 2px); left: 0; z-index: 1050; width: 100%; background-color: #fff; border: 1px solid #80bdff; border-bottom-left-radius: 0.25rem; border-bottom-right-radius: 0.25rem; }
.dropdown-list { max-height: 250px; overflow-y: auto; padding: 4px 0; }
.dropdown-item-custom { display: flex; align-items: center; padding: 6px 12px; margin: 0; cursor: pointer; font-weight: 400; font-size: 1rem; color: #000000; transition: background-color 0.15s; gap: 8px; }
.dropdown-item-custom:hover { background-color: #f8f9fa; }
.checkbox-ui { width: 15px; height: 15px; margin: 0; cursor: pointer; }
.dropdown-list::-webkit-scrollbar { width: 6px; }
.dropdown-list::-webkit-scrollbar-thumb { background: #c1c1c1; border-radius: 4px; }
.dropdown-list::-webkit-scrollbar-thumb:hover { background: #a8a8a8; }
.skeleton-loader { width: 100%; height: 1.2em; background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 4px; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.card-body.table-responsive { max-height: 75vh; overflow-y: auto; border-bottom: 1px solid #dee2e6; -webkit-overflow-scrolling: touch; }
.table thead th { position: sticky; top: 0; background-color: #f4f6f9; z-index: 10; box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1); }
@media (max-width: 768px) {
  .card-body .row>.col-12 { flex: 0 0 50%; max-width: 50%; }
  .form-control { font-size: 13px; padding: 0.25rem 0.5rem; height: calc(1.8125rem + 2px); }
  .form-group label { font-size: 12px; margin-bottom: 2px; }
  .card-footer { display: flex; flex-direction: column; gap: 10px; }
  .card-footer .btn { width: 100%; margin-left: 0 !important; }
  .table { font-size: 13px; }
  .table th, .table td { padding: 8px 6px; }
  .pagination .page-link { padding: 0.25rem 0.5rem; font-size: 12px; }
}
@media (min-width: 768px) and (max-width: 1024px) { .table { font-size: 13px; } }
</style>