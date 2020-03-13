import React, {useEffect, useState} from 'react'

const keys = {};
export const useSharedState = (key, initialValue) => {
    const [state, setState] = useState(undefined);
    useEffect(() => {
        if (!keys[key]) {
            keys[key] = {
                value: initialValue,
                setStates: []
            }
        }

        if (!keys[key].setStates.includes(setState)) {
            keys[key].setStates.push(setState)
        }
        setState(keys[key].value);

        return () => {
            const index = keys[key].setStates.indexOf(setState);
            keys[key].setStates.splice(index, 1)
        }
    }, []);


    const handleStateChange = (newValue) => {
        keys[key].value = newValue;
        keys[key].setStates.forEach(setState => setState(newValue));
    };

    console.log(keys);
    return [state, handleStateChange]
};