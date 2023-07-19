start:
	docker-compose up -d

stop:
	docker-compose rm --stop --force

reset:
	docker-compose down -v --remove-orphans
	docker-compose up -d