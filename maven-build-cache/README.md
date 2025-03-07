
```bash
 docker run --rm --entrypoint htpasswd httpd:2 -Bbn $REMOTE_CACHE_USER $REMOTE_CACHE_PASSWORD > maven-build-cache/nginx.htpasswd
 
```
```bash
docker run -p 8088:80 \                                                                                     15s  10:36:18
-v ./nginx.htpasswd:/etc/nginx/conf.d/nginx.htpasswd:ro \
-v ./nginx.conf:/etc/nginx/nginx.conf:ro \
-v ./www:/var/www \
nginx
```
