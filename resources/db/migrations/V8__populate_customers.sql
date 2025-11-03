INSERT INTO customer (
    names, 
    first_surname, 
    second_surname, 
    email, 
    country_of_birth, 
    birthday, 
    gender, 
    rfc, 
    curp, 
    password, 
    role, 
    phone_number, 
    phone_code, 
    country_code, 
    activation_code, 
    active,
    registration_date
) VALUES 
('María Elena', 'García', 'López', 'maria.garcia@email.com', 'MEX', '1990-05-15', 'FEMALE', 'GALM900515ABC', 
    'GALM900515MDFLRR01', '$2a$10$hashedpassword123', 'CUSTOMER', '+525512345678', '+52', 'MX', 'ACT123456', true,
    '2024-01-15 10:30:00'
),
('Carlos Antonio', 'Rodríguez', 'Martínez', 'carlos.rodriguez@email.com', 'MEX', '1985-08-22', 'MALE', 'ROCM850822HDF', 
    'ROCM850822HDFZRR02', '$2a$10$hashedpassword456', 'CUSTOMER', '+525598765432', '+52', 'MX', 'ACT789012', true,
    '2024-01-20 14:45:00'
),
('Ana Patricia', 'Hernández', 'Gómez', 'ana.hernandez@email.com', 'USA', '1992-11-30', 'FEMALE', 'HEGA921130USF', 
    'HEGA921130MDFRRN03', '$2a$10$hashedpassword789', 'ADMIN', '+13125551234', '+1', 'US', 'ACT345678', true,
    '2024-02-05 09:15:00'
),
('Luis Fernando', 'Pérez', 'Ramírez', 'luis.perez@email.com', 'ESP', '1988-03-10', 'MALE', 'PERL880310ESP', 
    'PERL880310HDFRRS04', '$2a$10$hashedpassword012', 'CUSTOMER', '+34600123456', '+34', 'ES', 'ACT901234', false,
    '2025-02-12 16:20:00'
),
('Sofía Isabel', 'Morales', 'Castillo', 'sofia.morales@email.com', 'COL', '1995-07-18', 'FEMALE', 'MOCS950718COL', 
    'MOCS950718MDFRST05', '$2a$10$hashedpassword345', 'CUSTOMER', '+573001234567', '+57', 'CO', 'ACT567890', true,
    '2025-02-18 11:00:00'
),
('Diego Alejandro', 'Silva', 'Reyes', 'diego.silva@email.com', 'MEX', '2003-02-14', 'MALE', 'SIRD030214MEX', 
    'SIRD030214HDFLJG06', '$2a$10$hashedpassword678', 'CUSTOMER', '+525511223344', '+52', 'MX', 'ACT112233', true,
    '2025-03-01 13:25:00'
),
('Valeria Michelle', 'Ortega', 'Vargas', 'valeria.ortega@email.com', 'ARG', '2002-09-08', 'FEMALE', 'ORGV020908ARG', 
    'ORGV020908MDFRTV07', '$2a$10$hashedpassword901', 'CUSTOMER', '+541155667788', '+54', 'AR', 'ACT445566', true,
    '2025-03-05 08:45:00'
),
('Roberto Carlos', 'Mendoza', 'Fuentes', 'roberto.mendoza@email.com', 'MEX', '1968-12-03', 'MALE', 'MEFR681203MEX', 
    'MEFR681203HDFNTR08', '$2a$10$hashedpassword234', 'CUSTOMER', '+525566778899', '+52', 'MX', 'ACT778899', true,
    '2025-03-10 17:30:00'
),
('Carmen Gloria', 'Delgado', 'Rojas', 'carmen.delgado@email.com', 'CHL', '1965-06-25', 'FEMALE', 'DERC650625CHL', 
    'DERC650625MDFLJR09', '$2a$10$hashedpassword567', 'CUSTOMER', '+562288776655', '+56', 'CL', 'ACT990011', true,
    '2025-03-15 12:10:00'
),
('Jorge Luis', 'Santos', 'Cervantes', 'jorge.santos@email.com', 'MEX', '1963-04-17', 'MALE', 'SACJ630417MEX', 
    'SACJ630417HDFNRV10', '$2a$10$hashedpassword890', 'CUSTOMER', '+525599887766', '+52', 'MX', 'ACT223344', true,
    '2025-03-20 15:40:00'
);
