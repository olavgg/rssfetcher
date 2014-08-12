DROP TABLE IF EXISTS news_article;
DROP TABLE IF EXISTS image;
DROP TABLE IF EXISTS content_type;

CREATE table content_type (
  id serial NOT NULL,
  description VARCHAR (1024),
  CONSTRAINT content_type_pk PRIMARY KEY (id)
);

CREATE table image (
  id serial NOT NULL,
  file_name varchar(4096) NOT NULL,
  file_path varchar (4096) NOT NULL,
  content_type int NOT NULL,
  FOREIGN KEY (content_type) REFERENCES content_type(id),
  CONSTRAINT image_pk PRIMARY KEY (id)
);

CREATE TABLE news_article (
  id serial NOT NULL,
  title varchar(4096)  NOT NULL,
  hash varchar(64) UNIQUE NOT NULL,
  description text NOT NULL,
  link varchar (4096)  NOT NULL,
  published_date timestamp NOT NULL,
  date_created timestamp NOT NULL,
  author varchar (512) NULL,
  image int NULL,
  FOREIGN KEY (image) REFERENCES image(id),
  CONSTRAINT news_articles_pk PRIMARY KEY (id)
);



