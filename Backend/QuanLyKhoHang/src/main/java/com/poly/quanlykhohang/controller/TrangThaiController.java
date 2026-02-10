package com.poly.quanlykhohang.controller;

import com.poly.quanlykhohang.dao.TrangThaiDAO;
import com.poly.quanlykhohang.entity.TrangThai;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trang-thai")
//@CrossOrigin(origins = "*")
public class TrangThaiController {

    @Autowired
    private TrangThaiDAO trangThaiDAO;

    @GetMapping
    public ResponseEntity<List<TrangThai>> getAll() {
        return ResponseEntity.ok(trangThaiDAO.findAll());
    }
}