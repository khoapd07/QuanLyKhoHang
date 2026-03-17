package com.poly.quanlykhohang.controller;
import com.poly.quanlykhohang.dao.HinhThucNhapDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hinh-thuc-nhap")
public class HinhThucNhapController {

    @Autowired
    private HinhThucNhapDAO hinhThucNhapDAO;

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(hinhThucNhapDAO.findAll());
    }
}