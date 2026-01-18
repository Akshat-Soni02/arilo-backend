**DB**
***For Prod***
We use cloud sql
***For Local***
start you local container with this

```bash
docker run --name pgvector-db \
  -e POSTGRES_PASSWORD=my_new_password \
  -p 5433:5432 \
  -d pgvector/pgvector:0.8.1-pg18-trixie
```

**Loading ENV**
To load the env variables, run this command
```bash
export $(cat .env | xargs)
```

**Running the app**
```bash
./gradlew bootRun
```
