/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.warmup.examples;

import java.util.List;

public interface Repository {
    void save(String data);
    String findById(String id);
    List<String> findAll();
}