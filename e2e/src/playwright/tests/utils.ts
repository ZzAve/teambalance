export const ensure = <T>(
  value: T | undefined,
  name: string | undefined = "value",
) => {
  return (
    value ??
    (() => {
      throw new Error(
        `Expected non-null values for '${name}' was unexpectedly null`,
      );
    })()
  );
};

export const HOST: string = ensure(process.env.HOST, "host");
export const PASSWORD = ensure(process.env.PASSWORD, "PASSWORD");
