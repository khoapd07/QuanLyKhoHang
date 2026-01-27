<template>
  <div class="dashboard">
    <h2>Tổng quan kho hàng</h2>
    
    <div class="row mt-4">
      <div class="col-md-3">
        <div class="card text-white bg-primary mb-3">
          <div class="card-header">Tổng sản phẩm</div>
          <div class="card-body">
            <h5 class="card-title">150 Mã</h5>
            <p class="card-text">Đang quản lý</p>
          </div>
        </div>
      </div>

      <div class="col-md-3">
        <div class="card text-white bg-success mb-3">
          <div class="card-header">Tồn kho</div>
          <div class="card-body">
            <h5 class="card-title">1,200 Máy</h5>
            <p class="card-text">Tổng số lượng</p>
          </div>
        </div>
      </div>

      <div class="col-md-3">
        <div class="card text-white bg-warning mb-3">
          <div class="card-header">Sắp hết hàng</div>
          <div class="card-body">
            <h5 class="card-title">5 Mã</h5>
            <p class="card-text">Cần nhập thêm</p>
          </div>
        </div>
      </div>
      
      <div class="col-md-3">
        <div class="card text-white bg-danger mb-3">
          <div class="card-header">Xuất trong tháng</div>
          <div class="card-body">
            <h5 class="card-title">32 Phiếu</h5>
            <p class="card-text">Hoạt động sôi nổi</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>