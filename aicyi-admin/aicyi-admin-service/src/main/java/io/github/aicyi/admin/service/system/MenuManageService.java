package io.github.aicyi.admin.service.system;

import io.github.aicyi.admin.dao.mapper.SysMenuMapper;
import io.github.aicyi.admin.domain.entity.SysMenu;
import io.github.aicyi.admin.domain.type.MenuType;
import io.github.aicyi.admin.domain.type.VisibleType;
import io.github.aicyi.admin.service.convert.ServiceConverter;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.aicyi.common.logging.Logger;
import io.github.aicyi.common.logging.LoggerFactory;
import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.BoBean;
import io.github.aicyi.common.model.type.BooleanType;
import io.github.aicyi.middleware.kit.util.IdUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单管理服务：树形展示 / 新增 / 编辑 / 删除 / 显示控制。
 *
 * <p>内置核心菜单（系统初始化菜单）禁止删除。
 */
@Service
public class MenuManageService {

    private static final Logger log = LoggerFactory.getLogger(MenuManageService.class);

    private final SysMenuMapper menuMapper;

    public MenuManageService(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /**
     * 菜单树形展示（目录 → 菜单 → 按钮三级）
     */
    public List<MenuNode> tree() {
        List<SysMenu> menus = menuMapper.selectList(
                Wrappers.<SysMenu>lambdaQuery().orderByAsc(SysMenu::getSort).orderByAsc(SysMenu::getId));
        Map<Long, MenuNode> nodeMap = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            nodeMap.put(menu.getId(), MenuNode.from(menu));
        }

        List<MenuNode> roots = new ArrayList<>();
        for (MenuNode node : nodeMap.values()) {
            MenuNode parent = nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        roots.sort(Comparator.comparingInt(MenuNode::getSort));
        return roots;
    }

    /**
     * 新增菜单 / 按钮（顶级菜单 parentId=0）
     */
    public SysMenu add(SysMenu menu) {
        menu.setId(IdUtils.generateId());
        menu.setBuiltin(BooleanType.FALSE);
        if (menu.getVisible() == null) {
            menu.setVisible(VisibleType.SHOW);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getMenuType() == null) {
            menu.setMenuType(MenuType.MENU);
        }
        menuMapper.insert(menu);
        log.info("menu_added menuId={} name={}", menu.getId(), menu.getMenuName());
        return menu;
    }

    /**
     * 编辑菜单：可调整排序、层级、显示状态、权限标识等
     */
    public SysMenu edit(SysMenu menu) {
        SysMenu existing = requireMenu(menu.getId());
        ServiceConverter.INSTANCE.updateMenu(existing, menu);
        menuMapper.updateById(existing);
        log.info("menu_edited menuId={}", menu.getId());
        return existing;
    }

    /**
     * 删除菜单：内置核心菜单禁止删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long menuId) {
        SysMenu menu = requireMenu(menuId);
        if (menu.getBuiltin() != null && menu.getBuiltin() == BooleanType.TRUE) {
            throw new IllegalArgumentException("系统内置菜单禁止删除: " + menu.getMenuName());
        }
        menuMapper.update(null, Wrappers.<SysMenu>lambdaUpdate()
                .eq(SysMenu::getId, menuId)
                .set(SysMenu::getDeleted, BooleanType.TRUE));
        log.info("menu_deleted menuId={}", menuId);
    }

    /**
     * 全部菜单（权限分配用，拉平列表）
     */
    public List<SysMenu> listAll() {
        return menuMapper.selectList(
                Wrappers.<SysMenu>lambdaQuery().orderByAsc(SysMenu::getSort).orderByAsc(SysMenu::getId));
    }

    private SysMenu requireMenu(Long menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在: menuId=" + menuId);
        }
        return menu;
    }

    /**
     * 菜单树节点（含子节点）
     */
    @Setter
    @Getter
    public static class MenuNode extends BaseBean implements BoBean {

        private Long id;
        private Long parentId;
        private String menuName;
        private MenuType menuType;
        private String path;
        private String icon;
        private Integer sort;
        private VisibleType visible;
        private String permCode;
        private String apiPath;
        private BooleanType builtin;
        private List<MenuNode> children = new ArrayList<>();

        public static MenuNode from(SysMenu menu) {
            return ServiceConverter.INSTANCE.toMenuNode(menu);
        }
    }
}
