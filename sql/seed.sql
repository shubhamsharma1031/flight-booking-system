-- ============================================================
-- Sample data. Flight dates are relative to today, so the
-- data always contains upcoming flights.
-- ============================================================
USE flight_booking;

INSERT INTO flights (flight_number, airline, source, destination, departure_time, arrival_time, total_seats, available_seats, fare) VALUES
('SW101', 'SkyWays',     'Jaipur',    'Delhi',     TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '06:30:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '07:35:00'), 180, 180, 3499.00),
('SW102', 'SkyWays',     'Delhi',     'Jaipur',    TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '19:10:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '20:15:00'), 180, 180, 3299.00),
('AB201', 'AirBharat',   'Jaipur',    'Mumbai',    TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '08:00:00'), TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '09:55:00'), 186, 186, 5299.00),
('AB202', 'AirBharat',   'Mumbai',    'Jaipur',    TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '17:45:00'), TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '19:40:00'), 186, 186, 5099.00),
('BL301', 'BlueLine',    'Delhi',     'Bengaluru', TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '10:15:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '13:05:00'), 220, 220, 6899.00),
('BL302', 'BlueLine',    'Bengaluru', 'Delhi',     TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '15:30:00'), TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '18:20:00'), 220, 220, 6699.00),
('SW401', 'SkyWays',     'Mumbai',    'Kolkata',   TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '07:20:00'), TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '10:00:00'), 180, 180, 7199.00),
('AB501', 'AirBharat',   'Chennai',   'Hyderabad', TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '12:00:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '13:15:00'), 150, 150, 3199.00),
('AB502', 'AirBharat',   'Hyderabad', 'Chennai',   TIMESTAMP(CURDATE() + INTERVAL 4 DAY, '09:40:00'), TIMESTAMP(CURDATE() + INTERVAL 4 DAY, '10:55:00'), 150, 150, 3099.00),
('BL601', 'BlueLine',    'Delhi',     'Mumbai',    TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '06:00:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '08:10:00'), 220, 220, 5599.00),
('BL602', 'BlueLine',    'Delhi',     'Mumbai',    TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '18:30:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '20:40:00'), 220, 4,   7999.00),
('SW701', 'SkyWays',     'Kolkata',   'Bengaluru', TIMESTAMP(CURDATE() + INTERVAL 5 DAY, '11:10:00'), TIMESTAMP(CURDATE() + INTERVAL 5 DAY, '13:50:00'), 180, 180, 7499.00),
('AB801', 'AirBharat',   'Jaipur',    'Bengaluru', TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '13:25:00'), TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '16:05:00'), 186, 186, 6499.00),
('BL901', 'BlueLine',    'Hyderabad', 'Delhi',     TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '21:00:00'), TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '23:15:00'), 220, 220, 5899.00),
('SW111', 'SkyWays',     'Delhi',     'Jaipur',    TIMESTAMP(CURDATE() + INTERVAL 6 DAY, '07:00:00'), TIMESTAMP(CURDATE() + INTERVAL 6 DAY, '08:05:00'), 180, 180, 2999.00);

INSERT INTO passengers (full_name, email, phone, age) VALUES
('Aarav Mehta',   'aarav.mehta@example.com',   '9876543210', 29),
('Priya Verma',   'priya.verma@example.com',   '9812345678', 34),
('Rohan Gupta',   'rohan.gupta@example.com',   '9988776655', 22);
