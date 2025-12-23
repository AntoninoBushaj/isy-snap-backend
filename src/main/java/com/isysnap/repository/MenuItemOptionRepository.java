package com.isysnap.repository;

import com.isysnap.entity.MenuItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemOptionRepository extends JpaRepository<MenuItemOption, String> {

    List<MenuItemOption> findByMenuItemId(String menuItemId);
}