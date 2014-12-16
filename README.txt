This assignment uses the following data model:

(a7.jpg)

The following is the SQL schema:

create table Grouping(
  id int primary key auto_increment
);
create table Product(
  id int primary key auto_increment,
  weight double not null,
  volume double not null
);
create table Invoice(
  id int primary key,
  shippedOn date not null,
  foreign key(id) references Grouping(id) on update cascade on delete cascade
);
create table Box(
  id int primary key,
  maximumWeight double not null,
  volume double not null,
  fulfills int,
  foreign key(id) references Grouping(id) on update cascade on delete cascade,
  foreign key(fulfills) references Invoice(id) on update cascade on delete no action
);
create table Containment(
  contains int,
  isContainedIn int,
  count int not null,
  primary key(contains, isContainedIn),
  foreign key(contains) references Product(id) on update cascade on delete cascade,
  foreign key(isContainedIn) references Grouping(id) on update cascade on delete cascade
);
In this database, one has invoices that represent customer orders for products. The containment association for an invoice specifies which products are in an invoice, and for each product how many items (instances) of the product are in the invoice. The containment association for a box specifies which products have been packaged in the box, and for each product how many items of the product are package in the box. Each item of a product has a weight and volume. Each box has a volume and a maximum weight that it can safely hold. Each invoice has a date when on which it is to be shipped.

Develop a Java program using JDBC that packages the items specified by the current invoices in the database. In other words, for every invoice that is to be shipped today, the products in the invoice must be placed in boxes. All boxes and invoices are already in the database. You must update the database with placements of products in boxes so that the total weight of the items in a box does not exceed the maximum for the box, and so that the total volume of the items in a box does not exceed the volume of the box. Indicate which invoice is fulfilled by each box that is used for the products in the invoice.

There is no requirement that your packaging be optimal, but try to put as many items in a box as possible. If you run out of boxes, then print an error message and end the program. You do not have to unpackage what was already packaged in this case.

Submit your assignment using a zip file containing your Java file(s).