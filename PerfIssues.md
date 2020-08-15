# Performance issues
Getting trainings take a long time.

```bash
$ curl 'http://localhost:3000/api/trainings?since=2010-08-14T03:28:08.159Z&include-attendees=true&limit=50' \
  -H 'accept: application/json' \
  -H 'x-secret: <secret>'
```

There are (at moment of testing) 21 trainings available:

## Initial impl:

Naive impl:
- get all trainings
- for each training, get attendees belonging to that training


Queries:
- get trainings O(1)
- for each training O(n)
    - get attendees O(1)
    - get all users from attendees O(n)

==> O(n<sup>2</sup>) 


| Processing times ||  
|---|---:|
| sample size:  |    50         |
| mean μ:       | 2416.72 ms    |
| std δ:        |   59.0 ms     |

     
## Optimizing call to fetch attendees (include users) in query directly
```kotlin
    @Query("SELECT a,u from Attendee a LEFT JOIN Uzer u on a.user.id = u.id  where a.event.id in :eventIds")
```

Queries:
- get trainings O(1)
- for each training O(n)
    - get attendees with all users O(1)

==> O(n) 


| Processing times ||  
|---|---:|
| sample size:  | 50         |
| mean μ:       | 302.7 ms    |
| std δ:        | 12 ms    |


Improvement comparision: factor >7,3 times faster

## Requesting all attendees for all trainings at once

Queries:
- get trainings O(1)
- get attendees for all trainings O(1)
    - Iterate over all unique users O(n)
- groupBy trainingId O(n) (list<attendee> --> Map<training-id, attendees>)
- Create response model, matching training to attendeelist O(1)

==> O(n) 

ℹ️ Locally grouping all attendees by training is apparently way cheaper than 
doing seperate db calls for each training to get attendees.

| Processing times ||  
|---|---:|
| sample size:  |  50|
| mean μ:       |  132.4 ms|
| std δ:        |  19.0 ms|

Improvement comparision: factor >15 times faster

## Combining Query optimizer and requesting all attendees at once
 ```kotlin
    @Query("SELECT a,u,e from Attendee a LEFT JOIN Uzer u on a.user.id = u.id LEFT JOIN Event e on a.event.id = e.id where a.event.id in :eventIds")
 ```

Queries:
- get trainings O(1)
- get attendees w/ userInfo  for all trainings O(1)
- groupBy trainingId O(n) (list<attendee> --> Map<training-id, attendees>)
- Create response model, matching training to attendeelist O(1)

==> O(n) 


 |Processing times ||  
|---|---:|
 |sample size:  |  50|
 |mean μ:       |  48.24 ms|
 |std δ:        |  14.0 ms|
 
 Improvement comparision: factor >35 times faster

 