import sys
import os
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.lib.units import inch
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_RIGHT, TA_JUSTIFY
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_number(num_pages)
            canvas.Canvas.showPage(self)
        canvas.Canvas.save(self)

    def draw_page_number(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748B"))
        
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 11 * inch - 36, "LANKA THREADS  |  Architecture & Directory Guide")
            self.drawRightString(8.5 * inch - 54, 11 * inch - 36, "Spring Boot & Thymeleaf Platform")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, 11 * inch - 42, 8.5 * inch - 54, 11 * inch - 42)
            
        # Footer
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(54, 45, 8.5 * inch - 54, 45)
        
        self.drawString(54, 32, "Confidential - Lanka Threads Enterprise Operations System")
        page_text = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(8.5 * inch - 54, 32, page_text)
        self.restoreState()

def build_pdf():
    pdf_filename = "Lanka_Threads_Architecture_Guide.pdf"
    doc = SimpleDocTemplate(
        pdf_filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    
    # Custom Palette
    GOLD = colors.HexColor("#C9A96E")
    DARK_BLUE = colors.HexColor("#1B2A4A")
    SLATE_900 = colors.HexColor("#0F172A")
    SLATE_700 = colors.HexColor("#334155")
    SLATE_500 = colors.HexColor("#64748B")
    BG_LIGHT = colors.HexColor("#F8FAFC")
    BORDER_COLOR = colors.HexColor("#E2E8F0")
    PURPLE = colors.HexColor("#6B21A8")
    EMERALD = colors.HexColor("#059669")
    
    # Typography Styles
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=24,
        leading=28,
        textColor=SLATE_900,
        spaceAfter=4
    )
    
    brand_sub_style = ParagraphStyle(
        'BrandSub',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10,
        leading=13,
        textColor=GOLD,
        textTransform='uppercase',
        spaceAfter=12
    )

    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=11,
        leading=16,
        textColor=SLATE_700,
        spaceAfter=15
    )

    h1_style = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=15,
        leading=18,
        textColor=DARK_BLUE,
        spaceBefore=14,
        spaceAfter=8,
        keepWithNext=True
    )

    h2_style = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Heading2'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=15,
        textColor=SLATE_900,
        spaceBefore=10,
        spaceAfter=6,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=SLATE_700,
        spaceAfter=6
    )

    bullet_style = ParagraphStyle(
        'Bullet_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=13.5,
        textColor=SLATE_700,
        leftIndent=15,
        spaceAfter=4
    )

    code_pill_style = ParagraphStyle(
        'CodePill',
        parent=styles['Normal'],
        fontName='Courier-Bold',
        fontSize=9,
        leading=11,
        textColor=DARK_BLUE
    )

    table_header_style = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8.5,
        leading=11,
        textColor=colors.white,
        alignment=TA_LEFT
    )

    table_cell_style = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.5,
        leading=12,
        textColor=SLATE_900
    )

    table_cell_mono = ParagraphStyle(
        'TableCellMono',
        parent=styles['Normal'],
        fontName='Courier-Bold',
        fontSize=8,
        leading=11,
        textColor=DARK_BLUE
    )

    story = []

    # --- Header Banner ---
    story.append(Paragraph("LANKA THREADS &nbsp;·&nbsp; ENTERPRISE SYSTEM", brand_sub_style))
    story.append(Paragraph("System Architecture & Folder Structure Guide", title_style))
    story.append(Paragraph("Comprehensive Technical Reference Manual for Spring Boot 3.2.5, Thymeleaf & MySQL Multi-Role ERP Platform", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=GOLD, spaceBefore=4, spaceAfter=14))

    # --- Metadata Table ---
    meta_data = [
        [
            Paragraph("<b>Framework:</b> Spring Boot 3.2.5", table_cell_style),
            Paragraph("<b>Language:</b> Java 17 / 24", table_cell_style),
            Paragraph("<b>Database:</b> MySQL (InnoDB, UTF8MB4)", table_cell_style)
        ],
        [
            Paragraph("<b>View Engine:</b> Thymeleaf + Tailwind CSS", table_cell_style),
            Paragraph("<b>Security:</b> Spring Security (Role-Based)", table_cell_style),
            Paragraph("<b>ORM:</b> Spring Data JPA / Hibernate", table_cell_style)
        ]
    ]
    meta_table = Table(meta_data, colWidths=[170, 170, 164])
    meta_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), BG_LIGHT),
        ('BOX', (0,0), (-1,-1), 1, BORDER_COLOR),
        ('INNERGRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('BOTTOMPADDING', (0,0), (-1,-1), 6),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(meta_table)
    story.append(Spacer(1, 14))

    # --- Section 1: Overview ---
    story.append(Paragraph("1. Enterprise Layered Architecture (4-Tier MVC)", h1_style))
    story.append(Paragraph(
        "Lanka Threads is organized according to the <b>Domain-Driven Layered Architecture</b>. "
        "Each business domain (such as <i>supplier</i>, <i>sales</i>, <i>inventory</i>, <i>manager</i>, <i>supervisor</i>, and <i>customer</i>) "
        "contains four synchronized layers. This separation of concerns ensures maintainability, testability, high throughput, and strict security boundaries.",
        body_style
    ))

    # Architecture Flow Box
    flow_data = [
        [
            Paragraph("<b>1. Controller Layer</b><br/><font color='#64748B'>Web Routing & Views</font>", table_cell_style),
            Paragraph("<b>&rarr;</b>", ParagraphStyle('Arrow', parent=styles['Normal'], alignment=TA_CENTER, fontName='Helvetica-Bold', fontSize=14, textColor=GOLD)),
            Paragraph("<b>2. Service Layer</b><br/><font color='#64748B'>Business Rules & Logic</font>", table_cell_style),
            Paragraph("<b>&rarr;</b>", ParagraphStyle('Arrow', parent=styles['Normal'], alignment=TA_CENTER, fontName='Helvetica-Bold', fontSize=14, textColor=GOLD)),
            Paragraph("<b>3. Repository Layer</b><br/><font color='#64748B'>Spring Data JPA / SQL</font>", table_cell_style),
            Paragraph("<b>&rarr;</b>", ParagraphStyle('Arrow', parent=styles['Normal'], alignment=TA_CENTER, fontName='Helvetica-Bold', fontSize=14, textColor=GOLD)),
            Paragraph("<b>4. Model Layer</b><br/><font color='#64748B'>MySQL Entities</font>", table_cell_style)
        ]
    ]
    flow_table = Table(flow_data, colWidths=[105, 20, 115, 20, 115, 20, 105])
    flow_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), BG_LIGHT),
        ('BOX', (0,0), (-1,-1), 1, DARK_BLUE),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(flow_table)
    story.append(Spacer(1, 14))

    # --- Section 2: Detailed Subfolder Explanation ---
    story.append(Paragraph("2. Layer-by-Layer Subfolder Guide & Responsibilities", h1_style))

    layers_info = [
        ("📁 controller", "Web Traffic Router & HTTP Request Dispatcher", [
            ("Listens to HTTP Endpoints", "Annotated with @Controller and @RequestMapping('/domain'). Defines @GetMapping, @PostMapping, and path variables."),
            ("Input & Parameter Binding", "Extracts form data, query strings, and multipart file uploads (e.g. employee photos, fabric receipts)."),
            ("Model & View Resolution", "Injects calculated attributes into org.springframework.ui.Model and returns the Thymeleaf template view name (e.g. 'supplier/suppliers')."),
            ("Flash Attributes & Security", "Attaches redirect success/error alerts via RedirectAttributes and enforces authenticated principal identity.")
        ]),
        ("📁 service", "Core Business Logic, Workflows & Decision Engine", [
            ("Business Rule Execution", "Performs business operations such as Quality Approval workflows, Fabric Stock automatic transfer, and BOM deductions."),
            ("Sequential Code Generation", "Thread-safe auto-generators for custom business IDs (e.g., SUP-001, MR-2026-001, PO-2026-001, WO-1001)."),
            ("Transaction Management", "Uses @Transactional annotations to ensure ACID compliance across multi-table operations (e.g. receiving fabric + logging audit trail)."),
            ("System Audit Logging", "Writes timestamped activity logs for compliance and transparency across all operational gates.")
        ]),
        ("📁 repository", "Database Query Layer (Spring Data JPA)", [
            ("JPA Repository Extension", "Extends JpaRepository<Entity, ID> to inherit out-of-the-box CRUD capabilities (.save(), .findAll(), .deleteById())."),
            ("Derived Query Methods", "Automatically generates optimized SQL queries based on method names (e.g., findByStatus, findBySupplierCode, findAllByOrderByTimestampDesc)."),
            ("Zero Boilerplate SQL", "Eliminates raw JDBC strings and SQL injection vulnerabilities through parameterized Hibernate queries.")
        ]),
        ("📁 model", "Database Entities & Domain Data Structures", [
            ("JPA Object-Relational Mapping", "Annotated with @Entity, @Table(name='...'), @Id, and @Column to directly reflect MySQL table schemas."),
            ("Domain Attributes & Constraints", "Defines data types (BigDecimal for currency, LocalDate for dates, Enums, Double for weights)."),
            ("Constructors & Encapsulation", "Provides clean getters, setters, and builder constructors for structured data passing.")
        ])
    ]

    for folder_title, summary, points in layers_info:
        story.append(Paragraph(f"<b>{folder_title}</b> &nbsp;—&nbsp; <font color='#64748B'>{summary}</font>", h2_style))
        for p_title, p_desc in points:
            story.append(Paragraph(f"• <b>{p_title}</b>: {p_desc}", bullet_style))
        story.append(Spacer(1, 4))

    story.append(Spacer(1, 10))

    # --- Section 3: Feature Modules Matrix ---
    story.append(Paragraph("3. Domain Modules & Business Responsibilities", h1_style))
    story.append(Paragraph("Each business module in <code>com.lankathreads.*</code> encapsulates a dedicated functional department:", body_style))

    modules_data = [
        [
            Paragraph("Module", table_header_style),
            Paragraph("Assigned Enterprise Role", table_header_style),
            Paragraph("Key Responsibilities & Capabilities", table_header_style)
        ],
        [
            Paragraph("<b>com.lankathreads.common</b>", table_cell_mono),
            Paragraph("All Users / System Admin", table_cell_style),
            Paragraph("Authentication, Spring Security (SecurityConfig), User entity, Profile photo management, Passwords.", table_cell_style)
        ],
        [
            Paragraph("<b>com.lankathreads.supplier</b>", table_cell_mono),
            Paragraph("Supplier Coordination Officer", table_cell_style),
            Paragraph("Fabric intake matrix, Purchase Orders, Quality Inspections (Approved/Rejected), Vendor Master Directory.", table_cell_style)
        ],
        [
            Paragraph("<b>com.lankathreads.inventory</b>", table_cell_mono),
            Paragraph("Inventory Officer", table_cell_style),
            Paragraph("Raw material roll stock, Color variations, Stock reorder thresholds, Wastage and usage tracking.", table_cell_style)
        ],
        [
            Paragraph("<b>com.lankathreads.supervisor</b>", table_cell_mono),
            Paragraph("Production Supervisor", table_cell_style),
            Paragraph("Daily worker shift assignments, Machine line schedules, Output logging, Defect rates.", table_cell_style)
        ],
        [
            Paragraph("<b>com.lankathreads.manager</b>", table_cell_mono),
            Paragraph("Factory Manager", table_cell_style),
            Paragraph("Executive operations overview, Worker directory management, Department productivity analytics.", table_cell_style)
        ],
        [
            Paragraph("<b>com.lankathreads.sales</b>", table_cell_mono),
            Paragraph("Sales Manager", table_cell_style),
            Paragraph("Polo product catalog, Bill of Materials (BOM), Customer bulk orders, Client account profiles.", table_cell_style)
        ],
        [
            Paragraph("<b>com.lankathreads.customer</b>", table_cell_mono),
            Paragraph("Customer (Storefront)", table_cell_style),
            Paragraph("Public polo collection catalog, Shopping cart, Online checkout, Real-time order tracking.", table_cell_style)
        ]
    ]

    modules_table = Table(modules_data, colWidths=[130, 120, 254])
    modules_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), DARK_BLUE),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(modules_table)
    story.append(Spacer(1, 14))

    # --- Section 4: Resources Directory Structure ---
    story.append(Paragraph("4. Resources Directory Structure (<code>src/main/resources</code>)", h1_style))
    
    res_data = [
        [
            Paragraph("Path / Directory", table_header_style),
            Paragraph("Type", table_header_style),
            Paragraph("Description & Purpose", table_header_style)
        ],
        [
            Paragraph("<b>templates/fragments/layout.html</b>", table_cell_mono),
            Paragraph("Thymeleaf Fragment", table_cell_style),
            Paragraph("Master application shell containing responsive navigation, profile dropdown, and theme toggle.", table_cell_style)
        ],
        [
            Paragraph("<b>templates/[role]/*.html</b>", table_cell_mono),
            Paragraph("Thymeleaf Views", table_cell_style),
            Paragraph("Role-specific dashboards and forms (e.g. <code>supplier/manage.html</code>, <code>supplier/suppliers.html</code>).", table_cell_style)
        ],
        [
            Paragraph("<b>static/css/style.css</b>", table_cell_mono),
            Paragraph("Stylesheet", table_cell_style),
            Paragraph("Custom CSS design tokens, 60fps hardware-accelerated transitions, and dark/light mode themes.", table_cell_style)
        ],
        [
            Paragraph("<b>static/images/</b>", table_cell_mono),
            Paragraph("Static Assets", table_cell_style),
            Paragraph("Polo collection photography, brand logos, and texture assets.", table_cell_style)
        ],
        [
            Paragraph("<b>application.properties</b>", table_cell_mono),
            Paragraph("Configuration", table_cell_style),
            Paragraph("MySQL database connection URL, credentials, server port, and JPA dialect properties.", table_cell_style)
        ]
    ]

    res_table = Table(res_data, colWidths=[160, 95, 249])
    res_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), DARK_BLUE),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, BG_LIGHT]),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(res_table)
    story.append(Spacer(1, 14))

    # --- Section 5: Architecture Summary Box ---
    summary_box_data = [
        [
            Paragraph(
                "<b>Key Architectural Takeaway:</b><br/>"
                "1. <b>Separation of Concerns:</b> Controllers never write SQL; Repositories never render HTML; Services orchestrate data flows.<br/>"
                "2. <b>Security First:</b> Role-based access ensures only authorized roles access designated endpoints.<br/>"
                "3. <b>Modularity:</b> Adding a new department requires only adding a package with <code>controller</code>, <code>service</code>, <code>repository</code>, and <code>model</code>.",
                table_cell_style
            )
        ]
    ]
    summary_box = Table(summary_box_data, colWidths=[504])
    summary_box.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), BG_LIGHT),
        ('BOX', (0,0), (-1,-1), 1.5, GOLD),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 12),
        ('RIGHTPADDING', (0,0), (-1,-1), 12),
    ]))
    story.append(summary_box)

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"Successfully generated {pdf_filename}")

if __name__ == '__main__':
    build_pdf()
