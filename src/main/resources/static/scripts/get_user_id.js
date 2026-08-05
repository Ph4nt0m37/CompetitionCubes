export let userIdPromise = fetch(`/api/get-user-data`).then((response)=> {
        if (response.ok)
            return response.json();
    }).then(user => {
        return user.userId;
    });