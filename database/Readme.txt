# Once launched the postgre container can be accessed

# Open an interactive terminal

docker exec -it container_id bash


# Connect with the db

psql -U dwh_postgres_user -d dwh_postgres_db

dwh_postgres_password

# List tables in the db

\dt


# See table content

SELECT * FROM table_name;
