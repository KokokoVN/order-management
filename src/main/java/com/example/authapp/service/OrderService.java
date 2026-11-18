package com.example.authapp.service;

import com.example.authapp.dto.*;
import com.example.authapp.entity.*;
import com.example.authapp.repository.CodeValueRepository;
import com.example.authapp.repository.OrderDetailRepository;
import com.example.authapp.repository.OrderRepository;
import com.example.authapp.repository.UserRepository;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CodeValueRepository codeValueRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderDetailRepository orderDetailRepository,
                        CodeValueRepository codeValueRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.codeValueRepository = codeValueRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // CODE VALUES (Giá trị mã)
    // ==========================================

    public List<CodeValueDto> getAllCodeValues() {
        return codeValueRepository.findAll().stream()
                .map(CodeValueDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CodeValueDto createCodeValue(CreateCodeValueRequest request) {
        String val = request.getCodeValue() != null ? request.getCodeValue().trim() : "";
        if (val.isEmpty()) {
            throw new IllegalArgumentException("Nội dung giá trị mã không được để trống!");
        }

        if (codeValueRepository.existsByCodeValue(val)) {
            throw new IllegalArgumentException("Giá trị mã '" + val + "' đã tồn tại!");
        }

        CodeValue saved = codeValueRepository.save(new CodeValue(val));
        return CodeValueDto.fromEntity(saved);
    }

    @Transactional
    public void deleteCodeValue(Long id) {
        CodeValue cv = codeValueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giá trị mã ID: " + id));

        // Rule: "Không xóa giá trị mã đang được sử dụng trong chi tiết đơn hàng"
        if (orderDetailRepository.existsByCodeValueId(id)) {
            throw new IllegalArgumentException("Không thể xóa giá trị mã '" + cv.getCodeValue() +
                    "' vì đang được sử dụng trong chi tiết đơn hàng!");
        }

        codeValueRepository.delete(cv);
    }

    // ==========================================
    // ORDERS (Đơn hàng & Chi tiết)
    // ==========================================

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getOrdersByUser(String username) {
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    public OrderDto getOrderById(Long orderId, String username, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!isAdmin && !order.getUser().getName().equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập đơn hàng này!");
        }

        return OrderDto.fromEntity(order);
    }

    @Transactional
    public OrderDto createOrder(String username, CreateOrderRequest request) {
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + username));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải chứa ít nhất 1 sản phẩm!");
        }

        // Lọc các item hợp lệ (bỏ qua các dòng hoàn toàn rỗng nếu người dùng thêm nhiều dòng mà chưa nhập)
        List<OrderDetailRequest> validItems = request.getItems().stream()
                .filter(it -> it != null && it.getProductName() != null && !it.getProductName().trim().isEmpty())
                .collect(Collectors.toList());

        if (validItems.isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng phải chứa ít nhất 1 link sản phẩm!");
        }

        // Tự động sinh mã đơn hàng nếu để trống
        String orderCode = request.getOrderCode();
        if (orderCode == null || orderCode.trim().isEmpty()) {
            orderCode = "DH" + String.format("%06d", (System.currentTimeMillis() % 1000000));
        } else {
            orderCode = orderCode.trim();
        }

        while (orderRepository.existsByOrderCode(orderCode)) {
            orderCode = "DH" + String.format("%06d", ((System.currentTimeMillis() + (int)(Math.random() * 10000)) % 1000000));
        }

        Order order = new Order(user, orderCode);
        order.setStatus(OrderStatus.da_tao);
        order.setCreatedAt(LocalDateTime.now());
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            order.setAddress(request.getAddress().trim());
        }

        Long orderLevelCodeValueId = request.getCodeValueId();

        for (OrderDetailRequest item : validItems) {
            int qty = (item.getQuantity() != null && item.getQuantity() > 0) ? item.getQuantity() : 1;

            Long effectiveCodeId = item.getCodeValueId() != null ? item.getCodeValueId() : orderLevelCodeValueId;
            if (effectiveCodeId == null) {
                // Fallback nếu không truyền mã: lấy mã đầu tiên trong database
                CodeValue firstCv = codeValueRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Hệ thống chưa có giá trị mã nào. Vui lòng liên hệ Admin để tạo giá trị mã trước khi đặt đơn!"));
                effectiveCodeId = firstCv.getId();
            }

            final Long finalCodeId = effectiveCodeId;
            CodeValue cv = codeValueRepository.findById(finalCodeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giá trị mã ID: " + finalCodeId));

            OrderDetail detail = new OrderDetail(
                    item.getProductName().trim(),
                    item.getVariant() != null ? item.getVariant().trim() : null,
                    qty,
                    cv
            );
            order.addDetail(detail);
        }

        Order saved = orderRepository.save(order);
        return OrderDto.fromEntity(saved);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus, LocalDateTime sentAt) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        order.setStatus(newStatus);
        if (sentAt != null) {
            order.setSentAt(sentAt);
        } else if (newStatus == OrderStatus.da_dat && order.getSentAt() == null) {
            order.setSentAt(LocalDateTime.now());
        }

        Order updated = orderRepository.save(order);
        return OrderDto.fromEntity(updated);
    }

    @Transactional
    public void deleteOrder(Long orderId, String username, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        if (!isAdmin) {
            if (!order.getUser().getName().equalsIgnoreCase(username)) {
                throw new IllegalArgumentException("Bạn không có quyền xóa đơn hàng này!");
            }
            if (order.getStatus() != OrderStatus.da_tao) {
                throw new IllegalArgumentException("Chỉ có thể xóa đơn hàng khi ở trạng thái 'Đã tạo'!");
            }
        }

        // Rule: "khi xóa đơn hàng, các dòng chi tiết của đơn đó được xóa theo"
        orderRepository.delete(order);
    }

    // ==========================================
    // XUẤT EXCEL DANH SÁCH & CHI TIẾT ĐƠN HÀNG
    // ==========================================
    public byte[] exportOrdersToExcel(OrderStatus statusFilter, String username, boolean isAdmin) throws IOException {
        List<Order> orders;
        if (isAdmin) {
            orders = orderRepository.findAllByOrderByCreatedAtDesc();
        } else {
            User user = userRepository.findByName(username)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        if (statusFilter != null) {
            orders = orders.stream()
                    .filter(o -> o.getStatus() == statusFilter)
                    .collect(Collectors.toList());
        }

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách đơn hàng");

            // Fonts
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);

            Font regularFont = workbook.createFont();
            regularFont.setFontHeightInPoints((short) 10);

            Font linkFont = workbook.createFont();
            linkFont.setUnderline(Font.U_SINGLE);
            linkFont.setColor(IndexedColors.BLUE.getIndex());
            linkFont.setFontHeightInPoints((short) 10);

            // Styles
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setWrapText(true);

            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setFont(regularFont);
            textStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.cloneStyleFrom(textStyle);
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle linkStyle = workbook.createCellStyle();
            linkStyle.cloneStyleFrom(textStyle);
            linkStyle.setFont(linkFont);

            // Status styles
            CellStyle statusTaoStyle = workbook.createCellStyle();
            statusTaoStyle.cloneStyleFrom(centerStyle);
            statusTaoStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            statusTaoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle statusDatStyle = workbook.createCellStyle();
            statusDatStyle.cloneStyleFrom(centerStyle);
            statusDatStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            statusDatStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle statusNhanStyle = workbook.createCellStyle();
            statusNhanStyle.cloneStyleFrom(centerStyle);
            statusNhanStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            statusNhanStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Headers
            String[] headers = {
                    "STT",
                    "Mã Đơn Hàng",
                    "Người Tạo",
                    "Địa Chỉ Đơn Hàng",
                    "Trạng Thái",
                    "Ngày Tạo",
                    "Ngày Gửi / Đặt",
                    "STT Sản Phẩm",
                    "Link Sản Phẩm",
                    "Phân Loại (Variant)",
                    "Số Lượng",
                    "Giá Trị Mã"
            };

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            CreationHelper createHelper = workbook.getCreationHelper();

            int rowIndex = 1;
            int orderStt = 1;

            for (Order order : orders) {
                List<OrderDetail> details = order.getOrderDetails();
                String orderCode = order.getOrderCode();
                String creator = order.getUser() != null ? order.getUser().getName() : "";
                String address = order.getAddress() != null ? order.getAddress() : "";
                String statusStr = order.getStatus() != null ? order.getStatus().getLabel() : "";
                String createdStr = order.getCreatedAt() != null ? order.getCreatedAt().format(dtf) : "";
                String sentStr = order.getSentAt() != null ? order.getSentAt().format(dtf) : "-";

                CellStyle currentStatusStyle = centerStyle;
                if (order.getStatus() == OrderStatus.da_tao) {
                    currentStatusStyle = statusTaoStyle;
                } else if (order.getStatus() == OrderStatus.da_dat) {
                    currentStatusStyle = statusDatStyle;
                } else if (order.getStatus() == OrderStatus.da_nhan_hang) {
                    currentStatusStyle = statusNhanStyle;
                }

                if (details == null || details.isEmpty()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.setHeightInPoints(20);

                    Cell c0 = row.createCell(0); c0.setCellValue(orderStt); c0.setCellStyle(centerStyle);
                    Cell c1 = row.createCell(1); c1.setCellValue(orderCode); c1.setCellStyle(centerStyle);
                    Cell c2 = row.createCell(2); c2.setCellValue(creator); c2.setCellStyle(textStyle);
                    Cell c3 = row.createCell(3); c3.setCellValue(address); c3.setCellStyle(textStyle);
                    Cell c4 = row.createCell(4); c4.setCellValue(statusStr); c4.setCellStyle(currentStatusStyle);
                    Cell c5 = row.createCell(5); c5.setCellValue(createdStr); c5.setCellStyle(centerStyle);
                    Cell c6 = row.createCell(6); c6.setCellValue(sentStr); c6.setCellStyle(centerStyle);

                    Cell c7 = row.createCell(7); c7.setCellValue("-"); c7.setCellStyle(centerStyle);
                    Cell c8 = row.createCell(8); c8.setCellValue("-"); c8.setCellStyle(textStyle);
                    Cell c9 = row.createCell(9); c9.setCellValue("-"); c9.setCellStyle(textStyle);
                    Cell c10 = row.createCell(10); c10.setCellValue(0); c10.setCellStyle(centerStyle);
                    Cell c11 = row.createCell(11); c11.setCellValue("-"); c11.setCellStyle(centerStyle);
                } else {
                    int itemIndex = 1;
                    for (OrderDetail item : details) {
                        Row row = sheet.createRow(rowIndex++);
                        row.setHeightInPoints(20);

                        Cell c0 = row.createCell(0); c0.setCellValue(orderStt); c0.setCellStyle(centerStyle);
                        Cell c1 = row.createCell(1); c1.setCellValue(orderCode); c1.setCellStyle(centerStyle);
                        Cell c2 = row.createCell(2); c2.setCellValue(creator); c2.setCellStyle(textStyle);
                        Cell c3 = row.createCell(3); c3.setCellValue(address); c3.setCellStyle(textStyle);
                        Cell c4 = row.createCell(4); c4.setCellValue(statusStr); c4.setCellStyle(currentStatusStyle);
                        Cell c5 = row.createCell(5); c5.setCellValue(createdStr); c5.setCellStyle(centerStyle);
                        Cell c6 = row.createCell(6); c6.setCellValue(sentStr); c6.setCellStyle(centerStyle);

                        Cell c7 = row.createCell(7); c7.setCellValue(itemIndex++); c7.setCellStyle(centerStyle);

                        String pName = item.getProductName() != null ? item.getProductName() : "";
                        Cell c8 = row.createCell(8);
                        if (pName.startsWith("http://") || pName.startsWith("https://")) {
                            c8.setCellValue(pName);
                            try {
                                Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
                                link.setAddress(pName);
                                c8.setHyperlink(link);
                            } catch (Exception ignored) {}
                            c8.setCellStyle(linkStyle);
                        } else {
                            c8.setCellValue(pName);
                            c8.setCellStyle(textStyle);
                        }

                        Cell c9 = row.createCell(9);
                        c9.setCellValue(item.getVariant() != null ? item.getVariant() : "");
                        c9.setCellStyle(textStyle);

                        Cell c10 = row.createCell(10);
                        c10.setCellValue(item.getQuantity() != null ? item.getQuantity() : 1);
                        c10.setCellStyle(centerStyle);

                        Cell c11 = row.createCell(11);
                        c11.setCellValue(item.getCodeValue() != null ? item.getCodeValue().getCodeValue() : "");
                        c11.setCellStyle(centerStyle);
                    }
                }
                orderStt++;
            }

            // Adjust column widths
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                if (width < 12 * 256) sheet.setColumnWidth(i, 12 * 256);
                if (width > 60 * 256) sheet.setColumnWidth(i, 60 * 256);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}

