First start up redis container:

```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

To try text analyzer send curl:

```bash
curl -X POST http://192.168.247.223:8080/api/v1/sentiment -d '{ "text": "Bad requset is always bad" }' -H "Content-Type: application/json"
```

If good, it will return response:

```json
{
  "id":"3f9d9f2d43b6dfb6084f9a9bca13af2d",
  "text":"Badwork2",
  "sentiment":"NEGATIVE",
  "score":0.82,
  "processingTimeMs":1000,
  "fromCache":false,
  "createdAt":"2026-05-07T17:53:40.6833314"
}
```

To get all ids:

```bash
curl http://192.168.247.223:8080/api/v1/sentiment/ids 
```

To get by id use *curl http://192.168.247.223:8080/api/v1/sentiment/{id}* where {id} is id:

```bash
curl http://192.168.247.223:8080/api/v1/sentiment/3a97458d1bbffbce08369431dbd1b6af
```

The returned result will be smth like that:

```json
{
  "id":"3a97458d1bbffbce08369431dbd1b6af",
  "text":"Badwork3",
  "sentiment":"NEGATIVE",
  "score":0.82,
  "processingTimeMs":1000,
  "fromCache":false,
  "createdAt":"2026-05-08T08:57:44.6640945"
}
```

Also, you may search by text:

```bash
curl 'http://192.168.247.223:8080/api/v1/sentiment/by-text?text=Badwork5' 
```

If your value not found, then IllegalArgumentException will be thrown with 500 status 