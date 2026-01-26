INSERT INTO category (id, name, parent_id, active) VALUES
(1, 'Electronics', NULL, true),
(2, 'Home & Kitchen', NULL, true),
(3, 'Fashion & Accessories', NULL, true),
(4, 'Health & Beauty', NULL, true),
(5, 'Sports & Outdoors', NULL, true),
(6, 'Toys & Games', NULL, true),
(7, 'Books & Entertainment', NULL, true),
(8, 'Supermarket', NULL, true),
(9, 'Automotive', NULL, true),
(10, 'Babies & Kids', NULL, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(11, 'Computers & Laptops', 1, true),
(12, 'Phones & Tablets', 1, true),
(13, 'Audio & Sound', 1, true),
(14, 'TVs & Video', 1, true),
(15, 'Video Games', 1, true),
(16, 'Cameras & Photography', 1, true),
(17, 'Wearables', 1, true),
(18, 'Electronic Accessories', 1, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(19, 'Laptops', 11, true),
(20, 'Desktop Computers', 11, true),
(21, 'Monitors', 11, true),
(22, 'PC Components', 11, true),
(23, 'Storage', 11, true),
(24, 'Printers & Scanners', 11, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(25, 'Smartphones', 12, true),
(26, 'Tablets', 12, true),
(27, 'Basic Phones', 12, true),
(28, 'Smartwatches', 12, false),
(29, 'Mobile Accessories', 12, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(30, 'Women''s Clothing', 3, true),
(31, 'Men''s Clothing', 3, true),
(32, 'Kids'' Clothing', 3, true),
(33, 'Footwear', 3, true),
(34, 'Bags & Luggage', 3, true),
(35, 'Watches & Jewelry', 3, true),
(36, 'Accessories', 3, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(37, 'Dresses', 30, true),
(38, 'Blouses & Shirts', 30, true),
(39, 'Pants & Jeans', 30, true),
(40, 'Skirts', 30, true),
(41, 'Jackets & Coats', 30, true),
(42, 'Sportswear', 30, true),
(43, 'Underwear', 30, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(44, 'Furniture', 2, true),
(45, 'Appliances', 2, true),
(46, 'Home Decor', 2, true),
(47, 'Kitchen & Utensils', 2, true),
(48, 'Bathroom', 2, true),
(49, 'Lighting', 2, true),
(50, 'Garden & Outdoor', 2, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(51, 'Refrigerators', 45, true),
(52, 'Washers & Dryers', 45, true),
(53, 'Microwaves', 45, true),
(54, 'Vacuums', 45, true),
(55, 'Air Conditioners', 45, true),
(56, 'Small Appliances', 45, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(57, 'Today''s Deals', NULL, true),
(58, 'New Products', NULL, true),
(59, 'Best Sellers', NULL, true),
(60, 'Clearance', NULL, true),
(61, 'Black Friday', NULL, false), -- Inactive until season
(62, 'Christmas', NULL, false),    -- Inactive until season
(63, 'Gifts', NULL, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(64, 'Electronics Deals', 57, true),
(65, 'Fashion Deals', 57, true),
(66, 'Home Deals', 57, true),
(67, 'Supermarket Deals', 57, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(68, 'Fitness', 5, true),
(69, 'Cycling', 5, true),
(70, 'Camping & Hiking', 5, true),
(71, 'Water Sports', 5, true),
(72, 'Team Sports', 5, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(73, 'Basic Foods', 8, true),
(74, 'Beverages', 8, true),
(75, 'Dairy & Eggs', 8, true),
(76, 'Meat & Fish', 8, true),
(77, 'Fruits & Vegetables', 8, true),
(78, 'Bakery & Pastry', 8, true),
(79, 'Snacks & Sweets', 8, true),
(80, 'Personal Care', 8, true),
(81, 'Home Cleaning', 8, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(82, 'Auto Parts', 9, true),
(83, 'Tools', 9, true),
(84, 'Car Accessories', 9, true),
(85, 'Lubricants & Fluids', 9, true),
(86, 'Tires', 9, true),
(87, 'Car Audio', 9, true);

INSERT INTO category (id, name, parent_id, active) VALUES
(88, 'Obsolete Technology', 1, false),
(89, 'Out of Season Fashion', 3, false),
(90, 'Discontinued Products', NULL, false);
