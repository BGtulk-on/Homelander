# Equipment Management System - Reporting Module

## Overview
This reporting system provides comprehensive equipment request analytics with PDF/CSV export capabilities using Factory pattern and external libraries.

## Features Implemented

### ✅ Reports Table
- **Primary Key**: Auto-generated ID
- **Foreign Keys**: fk_equipment, fk_user, fk_request
- **Purpose**: Links users, equipment, and requests for reporting relationships

### ✅ Report Endpoints

#### 1. GET /reports/usage
- **Purpose**: Usage statistics summary
- **Returns**: JSON with total returned requests, unique users, unique equipment
- **Example**: `{"totalReturnedRequests": 4, "uniqueUsers": 2, "uniqueEquipment": 4}`

#### 2. GET /reports/history
- **Purpose**: Complete request history
- **Returns**: JSON array of all returned requests with full details
- **Example**: `{"history": [{"userName": "Maria Georgieva", "equipmentName": "Dell XPS 13", ...}]}`

#### 3. GET /reports/export?format=csv|pdf&type=user|equipment
- **Purpose**: Generate and download reports
- **Parameters**:
  - `format`: "csv" or "pdf"
  - `type`: "user" or "equipment"
- **Returns**: JSON with `reportUrl` for accessing the generated file
- **Example**: `{"reportUrl": "/reports/user_report_20250321_143022.csv"}`

#### 4. GET /reports/user/export?format=csv|pdf
- **Purpose**: User-specific reports
- **Returns**: URL to user equipment request history

#### 5. GET /reports/equipment/export?format=csv|pdf
- **Purpose**: Equipment-specific reports
- **Returns**: URL to equipment request history

### ✅ Report Types

#### User Report
For each user, extracts:
- Equipment details requested
- Request date and return deadline
- Actual return date
- Condition of returned item

#### Equipment Report
For each equipment, extracts:
- Who requested the item
- Request date and return deadline
- Actual return date
- Condition of returned item

### ✅ Implementation Details

#### Factory Pattern
- **ReportFactory**: Centralized report generation with file management
- **Methods**: `generateReport()`, `generateUserReport()`, `generateEquipmentReport()`
- **File Management**: Automatic directory creation and timestamped filenames

#### External Libraries
- **OpenPDF**: PDF generation (`com.github.librepdf:openpdf:2.0.3`)
- **Apache Commons CSV**: CSV export (`org.apache.commons:commons-csv:1.11.0`)

#### File Storage
- **Directory**: `src/main/resources/reports/`
- **Access**: Served via `/reports/**` endpoint
- **Naming**: `{type}_report_{timestamp}.{format}`

#### DTOs Used
- **ReportRowDto**: Structured data for report rows
- **UserLoginDto**: User management (already existed)
- **UserRegisterDto**: User management (already existed)

### ✅ Database Integration
- **PostgreSQL Compatible**: Works with existing pgAdmin setup
- **No Database Changes**: Uses existing Reports table structure
- **Foreign Key Relationships**: Proper linking between entities

## Usage Examples

### Generate User CSV Report
```bash
GET /reports/export?format=csv&type=user
# Response: {"reportUrl": "/reports/user_report_20250321_143022.csv"}
```

### Generate Equipment PDF Report
```bash
GET /reports/export?format=pdf&type=equipment
# Response: {"reportUrl": "/reports/equipment_report_20250321_143022.pdf"}
```

### Access Generated Report
```bash
GET /reports/user_report_20250321_143022.csv
# Downloads the CSV file
```

### Get Usage Statistics
```bash
GET /reports/usage
# Response: {"totalReturnedRequests": 4, "uniqueUsers": 2, "uniqueEquipment": 4}
```

## Architecture

### Components
1. **ReportController**: REST endpoints with Factory pattern
2. **ReportFactory**: File generation and management
3. **ReportService**: Business logic and data processing
4. **ReportRowDto**: Data transfer object
5. **WebConfig**: Static file serving configuration

### Data Flow
```
Request → Controller → Factory → Service → Repository → Database
                ↓
            File Generation → Reports Directory → URL Response
```

## Requirements Met

✅ **Reports table** with PK and FKs  
✅ **All required endpoints** implemented  
✅ **User reports** with equipment details  
✅ **Equipment reports** with request details  
✅ **PDF/CSV export** using external libraries  
✅ **Factory pattern** for controller  
✅ **External library** for report generation  
✅ **Reports directory** in resources  
✅ **URL-based access** to generated reports  

## Notes
- System works with existing pgAdmin PostgreSQL setup
- No database modifications required
- User management DTOs already integrated
- Production-ready with proper error handling

## Dependencies Added
- `com.github.librepdf:openpdf:2.0.3` - PDF generation
- `org.apache.commons:commons-csv:1.11.0` - CSV export
- `org.springframework.boot:spring-boot-starter-validation` - Validation support
