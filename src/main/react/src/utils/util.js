export const delay = ms => new Promise(res => setTimeout(res, ms));


export const withLoading= async (loadingStateSetter, func) => {
    try{
        await loadingStateSetter(true);
        const x = await func(); // fault tolerant part
        console.debug(`Result of fun: ${x}`);
        return x
    // }
    // catch (e) {
    //     console.error("Exception occurred by 'loading surrounded' func.",e);
    //     throw e
    } finally {
        await loadingStateSetter(false);
    }
};