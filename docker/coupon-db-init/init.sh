#!/bin/bash

mongo=( mongosh --host mongo --port 27017 --quiet )

tries=30
while true; do
	sleep 1
	if "${mongo[@]}" --eval 'quit(0)' &> /dev/null; then
		# success!
		break
	fi
	(( tries-- ))
	if [ "$tries" -le 0 ]; then
		echo >&2
		echo >&2 'error: unable to initialize db'
		echo >&2
		kill -STOP 1 # initdb won't be executed twice, so fail loudly
		exit 1
	fi
done

echo 'about to init db as replica set'
"${mongo[@]}" <<-EOF
	rs.initiate({
	  _id: "coupon-db-rs",
	  version: 1,
	  members: [
	    { _id: 0, host : "localhost:27017" },
	  ]
	});
EOF