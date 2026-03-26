export const sanitizeInput = (val) => {
  if (typeof val !== 'string') return val;

  let sanitized = val;

  sanitized = sanitized
    .replace(/<script\b[^>]*>([\s\S]*?)<\/script>/gim, "")
    .replace(/on\w+="[^"]*"/gim, "")
    .replace(/on\w+='[^']*'/gim, "");

  sanitized = sanitized
    .replace(/'/g, "''")
    .replace(/;/g, "")
    .replace(/--/g, "")
    .replace(/\/\*/g, "")
    .replace(/\*\//g, "");

  return sanitized;
};

export const isSuspicious = (val) => {
  if (typeof val !== 'string') return false;

  const suspiciousPatterns = [
    /<script/i,
    /javascript:/i,
    /UNION SELECT/i,
    /DROP TABLE/i,
    /INSERT INTO/i,
    /OR 1=1/i,
    /--/
  ];

  return suspiciousPatterns.some(pattern => pattern.test(val));
};
